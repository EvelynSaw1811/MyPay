package com.mypay.collection.service.impl;

import com.mypay.collection.client.AuthClient;
import com.mypay.collection.dto.*;
import com.mypay.collection.entity.Collection;
import com.mypay.collection.entity.CollectionMember;
import com.mypay.collection.entity.Expense;
import com.mypay.collection.entity.ExpenseShare;
import com.mypay.collection.mapper.CollectionMapper;
import com.mypay.collection.repository.CollectionMemberRepository;
import com.mypay.collection.repository.CollectionRepository;
import com.mypay.collection.repository.ExpenseRepository;
import com.mypay.collection.repository.ExpenseShareRepository;
import com.mypay.collection.service.CollectionService;
import com.mypay.common.constant.CollectionRole;
import com.mypay.common.constant.CollectionStatus;
import com.mypay.common.exception.BadRequestException;
import com.mypay.common.exception.ForbiddenException;
import com.mypay.common.exception.ResourceNotFoundException;
import com.mypay.common.util.MoneyUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionServiceImpl implements CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionMemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository shareRepository;
    private final CollectionMapper collectionMapper;
    private final AuthClient authClient;

    @Override
    @Transactional
    public CollectionResponse createCollection(String userId, CreateCollectionRequest request) {
        Collection collection = Collection.builder()
                .collectionName(request.getName())
                .collectionDescription(request.getDescription())
                .collectionCategory(request.getCategory())
                .collectionTypeName(normalizeTypeName(request.getTypeName(), request.getCategory().name()))
                .collectionCurrency(request.getCurrency())
                .collectionOwnerId(userId)
                .build();
        collection = collectionRepository.save(collection);

        CollectionMember ownerMember = CollectionMember.builder()
                .collection(collection)
                .collectionMemberUserId(userId)
                .collectionMemberRole(CollectionRole.ADMIN)
                .build();
        memberRepository.save(ownerMember);

        return collectionMapper.toResponse(collection, CollectionRole.ADMIN);
    }

    @Override
    public List<CollectionResponse> getMyCollections(String userId) {
        return memberRepository.findByCollectionMemberUserId(userId).stream()
                .map(m -> collectionMapper.toResponse(
                        m.getCollection(),
                        m.getCollectionMemberRole(),
                        getNetBalance(m.getCollection(), userId)))
                .toList();
    }

    @Override
    public CollectionResponse getCollection(String collectionId, String userId) {
        Collection collection = findCollection(collectionId);
        CollectionRole myRole = memberRepository
                .findByCollectionAndCollectionMemberUserId(collection, userId)
                .map(CollectionMember::getCollectionMemberRole)
                .orElse(null);
        LocalDateTime lastSettledAt = shareRepository
                .findLastSettledDateTimeByCollection(collection)
                .orElse(null);
        return collectionMapper.toResponse(collection, myRole, getNetBalance(collection, userId), lastSettledAt);
    }

    @Override
    @Transactional
    public CollectionResponse updateCollection(String collectionId, CreateCollectionRequest request) {
        Collection collection = findCollection(collectionId);
        collection.setCollectionName(request.getName());
        collection.setCollectionDescription(request.getDescription());
        collection.setCollectionCategory(request.getCategory());
        collection.setCollectionTypeName(normalizeTypeName(request.getTypeName(), request.getCategory().name()));
        collection.setCollectionCurrency(request.getCurrency());
        return collectionMapper.toResponse(collectionRepository.save(collection));
    }

    @Override
    @Transactional
    public void closeCollection(String collectionId, String userId) {
        Collection collection = findCollection(collectionId);
        if (!collection.getCollectionOwnerId().equals(userId)) {
            throw new ForbiddenException("Only the owner can close a collection");
        }
        collection.setCollectionStatus(CollectionStatus.CLOSED);
        collectionRepository.save(collection);
    }

    @Override
    public List<MemberResponse> getMembers(String collectionId) {
        Collection collection = findCollection(collectionId);
        List<CollectionMember> members = memberRepository.findByCollection(collection);
        Map<String, Map<String, Object>> userInfo = resolveUsers(
                members.stream().map(CollectionMember::getCollectionMemberUserId).collect(Collectors.toSet())
        );
        return members.stream()
                .map(m -> {
                    Map<String, Object> info = userInfo.get(m.getCollectionMemberUserId());
                    return collectionMapper.toMemberResponse(m, displayNickname(info), stringValue(info, "invitationCode"));
                })
                .toList();
    }

    @Override
    @Transactional
    public void removeMember(String collectionId, String targetUserId, String requesterId) {
        Collection collection = findCollection(collectionId);
        CollectionMember requester = memberRepository.findByCollectionAndCollectionMemberUserId(collection, requesterId)
                .orElseThrow(() -> new ForbiddenException("You are not a member of this collection"));
        if (requester.getCollectionMemberRole() != CollectionRole.ADMIN) {
            throw new ForbiddenException("Only admins can remove members");
        }
        if (targetUserId.equals(collection.getCollectionOwnerId())) {
            throw new BadRequestException("Cannot remove the collection owner");
        }
        CollectionMember target = memberRepository.findByCollectionAndCollectionMemberUserId(collection, targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + targetUserId));
        memberRepository.delete(target);
    }

    @Override
    public List<BalanceSummaryResponse> getBalances(String collectionId, String userId) {
        Collection collection = findCollection(collectionId);
        List<CollectionMember> members = memberRepository.findByCollection(collection);
        List<Expense> allExpenses = expenseRepository.findByCollection(collection);

        Map<String, Map<String, Object>> userInfo = resolveUsers(
                members.stream().map(CollectionMember::getCollectionMemberUserId).collect(Collectors.toSet())
        );

        return members.stream().map(member -> {
            String uid = member.getCollectionMemberUserId();

            // What this member owes others: their unsettled shares in expenses PAID BY someone else
            BigDecimal totalOwed = shareRepository
                    .findByExpenseShareUserIdAndExpense_Collection_CollectionId(uid, collectionId)
                    .stream()
                    .filter(s -> !Boolean.TRUE.equals(s.getExpenseShareSettled()))
                    .filter(s -> !s.getExpense().getExpensePaidBy().equals(uid))
                    .map(ExpenseShare::getExpenseShareTotalAmount)
                    .reduce(BigDecimal.ZERO, MoneyUtil::add);

            // What others owe this member: unsettled shares of OTHER members in expenses PAID BY this member
            BigDecimal totalOwing = allExpenses.stream()
                    .filter(e -> uid.equals(e.getExpensePaidBy()))
                    .flatMap(e -> shareRepository.findByExpense(e).stream())
                    .filter(s -> !Boolean.TRUE.equals(s.getExpenseShareSettled()))
                    .filter(s -> !s.getExpenseShareUserId().equals(uid))
                    .map(ExpenseShare::getExpenseShareTotalAmount)
                    .reduce(BigDecimal.ZERO, MoneyUtil::add);

            Map<String, Object> info = userInfo.get(uid);
            return BalanceSummaryResponse.builder()
                    .userId(uid)
                    .totalOwed(totalOwed)
                    .totalOwing(totalOwing)
                    .netBalance(MoneyUtil.subtract(totalOwing, totalOwed))
                    .userNickname(displayNickname(info))
                    .invitationCode(stringValue(info, "invitationCode"))
                    .build();
        }).toList();
    }

    private Collection findCollection(String collectionId) {
        return collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found: " + collectionId));
    }

    private String normalizeTypeName(String requestedTypeName, String fallback) {
        if (requestedTypeName == null || requestedTypeName.isBlank()) {
            return fallback;
        }
        return requestedTypeName.trim();
    }

    private BigDecimal getNetBalance(Collection collection, String userId) {
        BigDecimal totalOwed = shareRepository
                .findByExpenseShareUserIdAndExpense_Collection_CollectionId(userId, collection.getCollectionId())
                .stream()
                .filter(s -> !Boolean.TRUE.equals(s.getExpenseShareSettled()))
                .filter(s -> !s.getExpense().getExpensePaidBy().equals(userId))
                .map(ExpenseShare::getExpenseShareTotalAmount)
                .reduce(BigDecimal.ZERO, MoneyUtil::add);

        BigDecimal totalOwing = expenseRepository.findByCollectionAndExpensePaidBy(collection, userId)
                .stream()
                .flatMap(e -> shareRepository.findByExpense(e).stream())
                .filter(s -> !Boolean.TRUE.equals(s.getExpenseShareSettled()))
                .filter(s -> !s.getExpenseShareUserId().equals(userId))
                .map(ExpenseShare::getExpenseShareTotalAmount)
                .reduce(BigDecimal.ZERO, MoneyUtil::add);

        return MoneyUtil.subtract(totalOwing, totalOwed);
    }

    /**
     * Look up display info for a set of user IDs via auth-service. Failures
     * for individual users are tolerated — they just won't have nickname/code
     * populated in the response.
     */
    private Map<String, Map<String, Object>> resolveUsers(Set<String> userIds) {
        Map<String, Map<String, Object>> out = new HashMap<>();
        for (String uid : userIds) {
            if (uid == null || uid.isBlank()) continue;
            try {
                var response = authClient.resolveUser(uid);
                if (response != null && response.getData() != null) {
                    out.put(uid, response.getData());
                }
            } catch (Exception ex) {
                log.debug("Failed to resolve user {}: {}", uid, ex.getMessage());
            }
        }
        return out;
    }

    private String stringValue(Map<String, Object> info, String key) {
        if (info == null) return null;
        Object v = info.get(key);
        return v == null ? null : v.toString();
    }

    /** Prefer nickname, then "First Last", then fall back to null. */
    private String displayNickname(Map<String, Object> info) {
        if (info == null) return null;
        String nickname = stringValue(info, "userNickname");
        if (nickname != null && !nickname.isBlank()) return nickname;
        String first = stringValue(info, "firstName");
        String last = stringValue(info, "lastName");
        String combined = ((first == null ? "" : first) + " " + (last == null ? "" : last)).trim();
        return combined.isBlank() ? null : combined;
    }
}
