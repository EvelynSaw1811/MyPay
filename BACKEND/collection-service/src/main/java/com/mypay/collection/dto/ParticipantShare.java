package com.mypay.collection.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ParticipantShare {

    @NotBlank(message = "Participant userId is required")
    private String userId;

    private BigDecimal percentage;
    private BigDecimal fixedAmount;
    private Integer shareWeight;
}
