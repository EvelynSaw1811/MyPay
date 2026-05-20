package com.mypay.collection.engine;

import com.mypay.common.util.MoneyUtil;

import java.math.BigDecimal;
import java.util.List;

public final class RemainderDistributor {

    private RemainderDistributor() {}

    public static void distribute(List<ShareResult> results, BigDecimal totalAmount) {
        if (results.isEmpty()) return;

        BigDecimal computedSum = results.stream()
                .map(ShareResult::getTotalAmount)
                .reduce(BigDecimal.ZERO, MoneyUtil::add);

        BigDecimal remainder = MoneyUtil.subtract(totalAmount, computedSum);

        if (remainder.compareTo(BigDecimal.ZERO) != 0) {
            ShareResult first = results.get(0);
            first.setTotalAmount(MoneyUtil.add(first.getTotalAmount(), remainder));
            first.setBaseAmount(MoneyUtil.add(first.getBaseAmount(), remainder));
        }
    }
}
