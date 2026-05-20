package com.mypay.reporting.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class CollectionStatementReport {
    private String collectionId;
    private String collectionName;
    private String category;
    private String currency;
    private String status;
    private BigDecimal totalAmount;
    private List<Map<String, Object>> expenses;
    private List<Map<String, Object>> memberBalances;
}
