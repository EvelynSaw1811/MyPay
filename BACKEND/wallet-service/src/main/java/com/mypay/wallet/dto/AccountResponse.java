package com.mypay.wallet.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AccountResponse {
    private String accountId;
    private String userId;
    private List<WalletResponse> wallets;
    /** Backward-compatible alias while older frontend callers migrate from accounts to wallets. */
    private List<WalletResponse> accounts;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
