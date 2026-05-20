package com.mypay.currency.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ExchangeRateTableResponse {
    private String baseCurrency;
    private Map<String, BigDecimal> rates;
    private LocalDateTime fetchedAt;
}
