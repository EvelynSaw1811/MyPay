package com.mypay.currency.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExchangeRateResponse {
    private String baseCurrency;
    private String targetCurrency;
    private BigDecimal rate;
    private LocalDateTime fetchedAt;
}
