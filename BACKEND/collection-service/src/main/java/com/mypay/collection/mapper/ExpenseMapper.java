package com.mypay.collection.mapper;

import com.mypay.collection.dto.ExpenseResponse;
import com.mypay.collection.dto.ShareResponse;
import com.mypay.collection.entity.Expense;
import com.mypay.collection.entity.ExpenseShare;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExpenseMapper {

    public ExpenseResponse toResponse(Expense expense, List<ExpenseShare> shares) {
        return ExpenseResponse.builder()
                .expenseId(expense.getExpenseId())
                .collectionId(expense.getCollection().getCollectionId())
                .title(expense.getExpenseTitle())
                .description(expense.getExpenseDescription())
                .amount(expense.getExpenseAmount())
                .currency(expense.getExpenseCurrency())
                .paidBy(expense.getExpensePaidBy())
                .createdBy(expense.getExpenseCreatedBy() != null ? expense.getExpenseCreatedBy() : expense.getExpensePaidBy())
                .splitType(expense.getExpenseSplitType())
                .taxRate(expense.getExpenseTaxRate())
                .taxType(expense.getExpenseTaxType())
                .shares(shares.stream().map(this::toShareResponse).toList())
                .createdAt(expense.getExpenseCreated())
                .build();
    }

    public ShareResponse toShareResponse(ExpenseShare share) {
        return ShareResponse.builder()
                .shareId(share.getExpenseShareId())
                .expenseId(share.getExpense().getExpenseId())
                .userId(share.getExpenseShareUserId())
                .baseAmount(share.getExpenseShareBaseAmount())
                .taxAmount(share.getExpenseShareTaxAmount())
                .totalAmount(share.getExpenseShareTotalAmount())
                .settled(Boolean.TRUE.equals(share.getExpenseShareSettled()))
                .settledAt(share.getExpenseShareSettledDateTime())
                .build();
    }
}
