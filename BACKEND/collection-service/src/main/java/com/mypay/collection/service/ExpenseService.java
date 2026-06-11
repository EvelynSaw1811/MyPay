package com.mypay.collection.service;

import com.mypay.collection.dto.CreateExpenseRequest;
import com.mypay.collection.dto.ExpenseResponse;
import com.mypay.collection.dto.ShareResponse;

import java.util.List;

public interface ExpenseService {
    ExpenseResponse createExpense(String collectionId, String userId, CreateExpenseRequest request);
    List<ExpenseResponse> getExpenses(String collectionId);
    ExpenseResponse getExpense(String collectionId, String expenseId);
    ExpenseResponse updateExpense(String collectionId, String expenseId, String userId, CreateExpenseRequest request);
    void deleteExpense(String collectionId, String expenseId, String userId, boolean waiveSettlements);
    ShareResponse getShare(String collectionId, String expenseId, String shareId);
    void settleShare(String collectionId, String expenseId, String shareId);
    void unsettleShare(String collectionId, String expenseId, String shareId);
}
