package com.mypay.reporting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class PersonalPositionReport {
    private BigDecimal totalOwed;
    private BigDecimal totalOwing;
    private BigDecimal totalReceivable;
    private BigDecimal totalPayable;
    private BigDecimal netPosition;
    private Map<String, BigDecimal> positionByCollection;
    private List<Map<String, Object>> positionsByCollection;
    private List<Map<String, Object>> byCollection;
    private List<Map<String, Object>> netDebts;
}
