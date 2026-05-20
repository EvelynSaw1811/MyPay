package com.mypay.transaction.dto;

import com.mypay.common.constant.TransactionStatus;
import com.mypay.common.constant.TransactionType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class TransactionResponse {
    private String transactionId;
    private String payerId;
    private String payeeId;
    private BigDecimal amount;
    private String currency;
    private TransactionType type;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
