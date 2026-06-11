package com.mypay.collection.service.impl;

import com.mypay.collection.dto.CreateExpenseRequest;
import com.mypay.collection.dto.ExpenseResponse;
import com.mypay.collection.dto.ShareResponse;
import com.mypay.collection.engine.*;
import com.mypay.collection.entity.*;
import com.mypay.collection.mapper.ExpenseMapper;
import com.mypay.collection.messaging.NotificationEventPublisher;
import com.mypay.collection.repository.*;
import com.mypay.collection.service.ExpenseService;
import com.mypay.common.constant.CollectionStatus;
import com.mypay.common.event.NotificationEvent;
import com.mypay.common.exception.BadRequestException;
import com.mypay.common.exception.ConflictException;
import com.mypay.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImpl implements ExpenseService {

    private static final int MAX_EXPENSES_PER_COLLECTION = 100;

    private final CollectionRepository collectionRepository;
    private final CollectionMemberRepository memberRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository shareRepository;
    private final SplitRuleRepository splitRuleRepository;
    private final SplitStrategyFactory strategyFactory;
    private final NotificationEventPublisher notificationPublisher;
    private final ExpenseMapper expenseMapper;

    @Override
    @Transactional
    public ExpenseResponse createExpense(String collectionId, String userId, CreateExpenseRequest request) {
        Collection collection = findCollection(collectionId);

        if (collection.getCollectionStatus() != CollectionStatus.ACTIVE) {
            throw new BadRequestException("Cannot add expenses to a closed collection");
        }

        if (!memberRepository.existsByCollectionAndCollectionMemberUserId(collection, userId)) {
            throw new BadRequestException("User is not a member of this collection");
        }
        if (!memberRepository.existsByCollectionAndCollectionMemberUserId(collection, request.getPaidBy())) {
            throw new BadRequestException("Payer is not a member of this collection");
        }
        if (expenseRepository.countByCollection(collection) >= MAX_EXPENSES_PER_COLLECTION) {
            throw new ConflictException("Collection can have at most " + MAX_EXPENSES_PER_COLLECTION + " expenses");
        }

        Expense expense = Expense.builder()
                .collection(collection)
                .expenseTitle(request.getTitle())
                .expenseDescription(request.getDescription())
                .expenseAmount(request.getAmount())
                .expenseCurrency(request.getCurrency())
                .expensePaidBy(request.getPaidBy())
                .expenseSplitType(request.getSplitType())
                .expenseTaxRate(request.getTaxRate())
                .expenseTaxType(request.getTaxType())
                .build();
        expense = expenseRepository.save(expense);

        for (var p : request.getParticipants()) {
            SplitRule rule = SplitRule.builder()
                    .expense(expense)
                    .splitRuleUserId(p.getUserId())
                    .splitRulePercentage(p.getPercentage())
                    .splitRuleFixedAmount(p.getFixedAmount())
                    .splitRuleWeight(p.getShareWeight())
                    .build();
            splitRuleRepository.save(rule);
        }

        SplitStrategy strategy = strategyFactory.get(request.getSplitType());
        List<ShareResult> results = strategy.calculate(request.getAmount(), request.getParticipants());

        if (request.getTaxRate() != null) {
            TaxCalculator.apply(results, request.getTaxRate(), request.getTaxType());
            // Distribute rounding remainder against the post-tax total, not the pre-tax amount
            BigDecimal postTaxTotal = results.stream()
                    .map(ShareResult::getTotalAmount)
                    .reduce(java.math.BigDecimal.ZERO, com.mypay.common.util.MoneyUtil::add);
            RemainderDistributor.distribute(results, postTaxTotal);
        }

        final Expense savedExpense = expense;
        List<ExpenseShare> shares = results.stream()
                .map(r -> {
                    boolean payerShare = r.getUserId().equals(request.getPaidBy());
                    return ExpenseShare.builder()
                            .expense(savedExpense)
                            .expenseShareUserId(r.getUserId())
                            .expenseShareBaseAmount(r.getBaseAmount())
                            .expenseShareTaxAmount(r.getTaxAmount())
                            .expenseShareTotalAmount(r.getTotalAmount())
                            .expenseShareSettled(payerShare)
                            .expenseShareSettledDateTime(null)
                            .build();
                })
                .toList();
        shareRepository.saveAll(shares);

        for (var share : shares) {
            if (!share.getExpenseShareUserId().equals(userId)) {
                notificationPublisher.publishExpenseCreated(NotificationEvent.builder()
                        .userId(share.getExpenseShareUserId())
                        .type("EXPENSE_CREATED")
                        .title("New expense added")
                        .message("You owe " + share.getExpenseShareTotalAmount() + " " + request.getCurrency()
                                + " for: " + request.getDescription())
                        .referenceId(expense.getExpenseId())
                        .build());
            }
        }

        return expenseMapper.toResponse(expense, shares);
    }

    @Override
    public List<ExpenseResponse> getExpenses(String collectionId) {
        Collection collection = findCollection(collectionId);
        return expenseRepository.findByCollection(collection).stream()
                .map(e -> expenseMapper.toResponse(e, shareRepository.findByExpense(e)))
                .toList();
    }

    @Override
    public ExpenseResponse getExpense(String collectionId, String expenseId) {
        Expense expense = findExpense(collectionId, expenseId);
        return expenseMapper.toResponse(expense, shareRepository.findByExpense(expense));
    }

    @Override
    @Transactional
    public ExpenseResponse updateExpense(String collectionId, String expenseId, String userId, CreateExpenseRequest request) {
        Expense expense = findExpense(collectionId, expenseId);
        final String existingPaidBy = expense.getExpensePaidBy();
        boolean hasSettledShares = shareRepository.findByExpense(expense).stream()
                .anyMatch(s -> Boolean.TRUE.equals(s.getExpenseShareSettled())
                        && !s.getExpenseShareUserId().equals(existingPaidBy));
        if (hasSettledShares) {
            throw new ConflictException("Cannot update expense with settled shares");
        }

        splitRuleRepository.deleteAll(splitRuleRepository.findByExpense(expense));
        shareRepository.deleteAll(shareRepository.findByExpense(expense));

        expense.setExpenseTitle(request.getTitle());
        expense.setExpenseDescription(request.getDescription());
        expense.setExpenseAmount(request.getAmount());
        expense.setExpenseCurrency(request.getCurrency());
        expense.setExpensePaidBy(request.getPaidBy());
        expense.setExpenseSplitType(request.getSplitType());
        expense.setExpenseTaxRate(request.getTaxRate());
        expense.setExpenseTaxType(request.getTaxType());
        expense = expenseRepository.save(expense);

        for (var p : request.getParticipants()) {
            splitRuleRepository.save(SplitRule.builder()
                    .expense(expense)
                    .splitRuleUserId(p.getUserId())
                    .splitRulePercentage(p.getPercentage())
                    .splitRuleFixedAmount(p.getFixedAmount())
                    .splitRuleWeight(p.getShareWeight())
                    .build());
        }

        SplitStrategy strategy = strategyFactory.get(request.getSplitType());
        List<ShareResult> results = strategy.calculate(request.getAmount(), request.getParticipants());
        if (request.getTaxRate() != null) {
            TaxCalculator.apply(results, request.getTaxRate(), request.getTaxType());
            BigDecimal postTaxTotal = results.stream()
                    .map(ShareResult::getTotalAmount)
                    .reduce(java.math.BigDecimal.ZERO, com.mypay.common.util.MoneyUtil::add);
            RemainderDistributor.distribute(results, postTaxTotal);
        }

        final Expense savedExpense = expense;
        List<ExpenseShare> shares = results.stream()
                .map(r -> {
                    boolean payerShare = r.getUserId().equals(request.getPaidBy());
                    return ExpenseShare.builder()
                            .expense(savedExpense)
                            .expenseShareUserId(r.getUserId())
                            .expenseShareBaseAmount(r.getBaseAmount())
                            .expenseShareTaxAmount(r.getTaxAmount())
                            .expenseShareTotalAmount(r.getTotalAmount())
                            .expenseShareSettled(payerShare)
                            .expenseShareSettledDateTime(null)
                            .build();
                })
                .toList();
        shareRepository.saveAll(shares);

        return expenseMapper.toResponse(expense, shares);
    }

    @Override
    @Transactional
    public void deleteExpense(String collectionId, String expenseId, String userId, boolean waiveSettlements) {
        Expense expense = findExpense(collectionId, expenseId);
        boolean hasUnsettledCounterpartyShares = shareRepository.findByExpense(expense).stream()
                .anyMatch(s -> !Boolean.TRUE.equals(s.getExpenseShareSettled())
                        && !s.getExpenseShareUserId().equals(expense.getExpensePaidBy()));
        if (hasUnsettledCounterpartyShares && !waiveSettlements) {
            throw new ConflictException("Expense has unsettled shares. Settle all shares or waive remaining settlements before deleting.");
        }
        expenseRepository.delete(expense);
    }

    @Override
    public ShareResponse getShare(String collectionId, String expenseId, String shareId) {
        Expense expense = findExpense(collectionId, expenseId);
        ExpenseShare share = shareRepository.findByExpenseShareIdAndExpense_ExpenseId(shareId, expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Share not found: " + shareId));
        return expenseMapper.toShareResponse(share);
    }

    @Override
    @Transactional
    public void settleShare(String collectionId, String expenseId, String shareId) {
        Expense expense = findExpense(collectionId, expenseId);
        ExpenseShare share = shareRepository.findByExpenseShareIdAndExpense_ExpenseId(shareId, expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Share not found: " + shareId));
        if (Boolean.TRUE.equals(share.getExpenseShareSettled())) {
            throw new ConflictException("Share already settled");
        }
        share.setExpenseShareSettled(true);
        share.setExpenseShareSettledDateTime(LocalDateTime.now());
        shareRepository.save(share);
    }

    @Override
    @Transactional
    public void unsettleShare(String collectionId, String expenseId, String shareId) {
        Expense expense = findExpense(collectionId, expenseId);
        ExpenseShare share = shareRepository.findByExpenseShareIdAndExpense_ExpenseId(shareId, expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Share not found: " + shareId));
        share.setExpenseShareSettled(false);
        share.setExpenseShareSettledDateTime(null);
        shareRepository.save(share);
    }

    private Collection findCollection(String collectionId) {
        return collectionRepository.findById(collectionId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found: " + collectionId));
    }

    private Expense findExpense(String collectionId, String expenseId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + expenseId));
        if (!expense.getCollection().getCollectionId().equals(collectionId)) {
            throw new ResourceNotFoundException("Expense not found in this collection");
        }
        return expense;
    }
}
