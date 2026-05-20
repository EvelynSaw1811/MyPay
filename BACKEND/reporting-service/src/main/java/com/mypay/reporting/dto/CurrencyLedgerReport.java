package com.mypay.reporting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CurrencyLedgerReport {
    private String currency;
    private BigDecimal totalInflow;
    private BigDecimal totalOutflow;
    private BigDecimal net;
}
