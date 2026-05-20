package com.mypay.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletOperationRequest {

    @NotBlank(message = "CurrencyCode is required")
    private String currency;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0001", message = "Amount must be positive")
    private BigDecimal amount;
}
