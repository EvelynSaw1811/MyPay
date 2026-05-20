package com.mypay.collection.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ShareResponse {
    private String shareId;
    private String expenseId;
    private String userId;
    private BigDecimal baseAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
    private boolean settled;
    private LocalDateTime settledAt;
}
