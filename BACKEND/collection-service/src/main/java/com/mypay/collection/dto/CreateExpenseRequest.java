package com.mypay.collection.dto;

import com.mypay.common.constant.SplitType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateExpenseRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 30, message = "Title must be at most 30 characters")
    private String title;

    @Size(max = 100, message = "Description must be at most 100 characters")
    private String description;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be positive")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    private String currency;

    @NotBlank(message = "Payer is required")
    private String paidBy;

    @NotNull(message = "Split type is required")
    private SplitType splitType;

    @NotEmpty(message = "At least one participant is required")
    private List<ParticipantShare> participants;

    private BigDecimal taxRate;
    private String taxType;
}
