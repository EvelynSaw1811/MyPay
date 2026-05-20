package com.mypay.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SettleRequest {

    @NotBlank(message = "Share ID is required")
    private String shareId;

    @NotBlank(message = "Collection ID is required")
    private String collectionId;

    @NotBlank(message = "Expense ID is required")
    private String expenseId;

    private String payeeCurrency;
    private String idempotencyKey;
}
