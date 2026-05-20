package com.mypay.collection.engine;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShareResult {
    private String userId;
    private BigDecimal baseAmount;
    private BigDecimal taxAmount;
    private BigDecimal totalAmount;
}
