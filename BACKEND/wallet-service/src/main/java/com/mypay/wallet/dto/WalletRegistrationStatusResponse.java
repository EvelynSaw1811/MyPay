package com.mypay.wallet.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WalletRegistrationStatusResponse {
    private String currency;
    private boolean walletExists;
    private boolean canRegister;
}
