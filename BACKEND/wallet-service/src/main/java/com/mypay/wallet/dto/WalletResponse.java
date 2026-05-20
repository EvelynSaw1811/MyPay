package com.mypay.wallet.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class WalletResponse {
    private String walletId;
    private String accountId;
    private String currency;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
