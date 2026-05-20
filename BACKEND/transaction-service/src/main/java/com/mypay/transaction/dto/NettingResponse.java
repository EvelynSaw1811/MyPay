package com.mypay.transaction.dto;

import com.mypay.common.constant.TransactionStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class NettingResponse {
    private String transactionId;
    private String netPayerId;
    private String netPayeeId;
    private BigDecimal netAmount;
    private String currency;
    private TransactionStatus status;
    private LocalDateTime createdAt;
}
