package com.mypay.reporting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class SpendingSummaryReport {
    private BigDecimal totalSpent;
    private BigDecimal totalReceived;
    private BigDecimal netBalance;
    private Map<String, BigDecimal> byType;
}
