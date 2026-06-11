package com.mypay.collection.dto;

import com.mypay.common.constant.SplitType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ExpenseResponse {
    private String expenseId;
    private String collectionId;
    private String title;
    private String description;
    private BigDecimal amount;
    private String currency;
    private String paidBy;
    private String createdBy;
    private SplitType splitType;
    private BigDecimal taxRate;
    private String taxType;
    private List<ShareResponse> shares;
    private LocalDateTime createdAt;
}
