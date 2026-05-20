package com.mypay.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OpenWalletRequest {

    @NotBlank(message = "CurrencyCode is required")
    private String currency;
}
