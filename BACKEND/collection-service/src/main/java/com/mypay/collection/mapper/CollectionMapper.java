package com.mypay.collection.mapper;

import com.mypay.collection.dto.CollectionResponse;
import com.mypay.collection.dto.MemberResponse;
import com.mypay.collection.entity.Collection;
import com.mypay.collection.entity.CollectionMember;
import com.mypay.common.constant.CollectionRole;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class CollectionMapper {

    public CollectionResponse toResponse(Collection collection) {
        return toResponse(collection, null);
    }

    public CollectionResponse toResponse(Collection collection, CollectionRole myRole) {
        return toResponse(collection, myRole, null);
    }

    public CollectionResponse toResponse(Collection collection, CollectionRole myRole, BigDecimal myNetBalance) {
        return toResponse(collection, myRole, myNetBalance, null);
    }

    public CollectionResponse toResponse(Collection collection, CollectionRole myRole, BigDecimal myNetBalance,
                                         LocalDateTime lastSettledAt) {
        return CollectionResponse.builder()
                .collectionId(collection.getCollectionId())
                .name(collection.getCollectionName())
                .description(collection.getCollectionDescription())
                .category(collection.getCollectionCategory())
                .typeName(collection.getCollectionTypeName())
                .currency(collection.getCollectionCurrency())
                .status(collection.getCollectionStatus())
                .ownerId(collection.getCollectionOwnerId())
                .createdAt(collection.getCollectionCreated())
                .updatedAt(collection.getCollectionUpdated())
                .lastSettledAt(lastSettledAt)
                .myRole(myRole)
                .myNetBalance(myNetBalance)
                .build();
    }

    public MemberResponse toMemberResponse(CollectionMember member) {
        return toMemberResponse(member, null, null);
    }

    public MemberResponse toMemberResponse(CollectionMember member, String userNickname, String invitationCode) {
        return MemberResponse.builder()
                .memberId(member.getCollectionMemberId())
                .userId(member.getCollectionMemberUserId())
                .role(member.getCollectionMemberRole())
                .joinedAt(member.getCollectionMemberJoinedDateTime())
                .userNickname(userNickname)
                .invitationCode(invitationCode)
                .build();
    }
}
