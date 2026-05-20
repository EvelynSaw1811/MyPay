package com.mypay.transaction.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExpenseShareInfo {
    private String shareId;
    private String expenseId;
    private String collectionId;
    private String userId;
    private BigDecimal totalAmount;
    private boolean settled;
}
