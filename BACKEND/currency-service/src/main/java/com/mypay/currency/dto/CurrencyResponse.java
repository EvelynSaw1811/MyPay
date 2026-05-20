package com.mypay.currency.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CurrencyResponse {
    private String code;
    private String name;
    private String symbol;
    private boolean active;
}
