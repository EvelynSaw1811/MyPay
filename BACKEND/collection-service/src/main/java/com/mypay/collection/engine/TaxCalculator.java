package com.mypay.collection.engine;

import com.mypay.common.util.MoneyUtil;

import java.math.BigDecimal;
import java.util.List;

public final class TaxCalculator {

    private TaxCalculator() {}

    public static void apply(List<ShareResult> results, BigDecimal taxRate, String taxType) {
        if (taxRate == null || taxRate.compareTo(BigDecimal.ZERO) == 0) return;

        for (ShareResult result : results) {
            BigDecimal taxAmount = computeTax(result.getBaseAmount(), taxRate, taxType);
            result.setTaxAmount(taxAmount);
            result.setTotalAmount(MoneyUtil.add(result.getBaseAmount(), taxAmount));
        }
    }

    private static BigDecimal computeTax(BigDecimal baseAmount, BigDecimal taxRate, String taxType) {
        if ("COMPOUND".equalsIgnoreCase(taxType)) {
            // Compound: tax = base * rate / (1 - rate)
            BigDecimal denominator = MoneyUtil.subtract(BigDecimal.ONE, taxRate);
            if (denominator.compareTo(BigDecimal.ZERO) <= 0) {
                return MoneyUtil.multiply(baseAmount, taxRate);
            }
            return MoneyUtil.divide(MoneyUtil.multiply(baseAmount, taxRate), denominator);
        }
        // SIMPLE: tax = base * rate
        return MoneyUtil.multiply(baseAmount, taxRate);
    }
}
