package com.mypay.transaction.dto;

import com.mypay.common.constant.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class SettlementResponse {
    private String transactionId;
    private String settlementId;
    private String payerId;
    private String payeeId;
    private BigDecimal amount;
    private String currency;
    private BigDecimal convertedAmount;
    private String payeeCurrency;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
