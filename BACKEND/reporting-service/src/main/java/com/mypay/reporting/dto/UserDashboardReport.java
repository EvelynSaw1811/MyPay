package com.mypay.reporting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class UserDashboardReport {
    private Map<String, Object> walletSummary;
    private Map<String, Object> walletBalances;
    private int collectionCount;
    private BigDecimal totalTransacted;
    private long transactionCount;
    private BigDecimal totalReceivable;
    private BigDecimal totalPayable;
    private BigDecimal netPosition;
}
