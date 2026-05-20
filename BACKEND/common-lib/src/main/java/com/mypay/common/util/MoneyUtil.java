package com.mypay.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtil {

    public static final int SCALE = 4;
    public static final RoundingMode ROUNDING = RoundingMode.HALF_UP;

    private MoneyUtil() {}

    public static BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b).setScale(SCALE, ROUNDING);
    }

    public static BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return a.subtract(b).setScale(SCALE, ROUNDING);
    }

    public static BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return a.multiply(b).setScale(SCALE, ROUNDING);
    }

    public static BigDecimal divide(BigDecimal a, BigDecimal b) {
        return a.divide(b, SCALE, ROUNDING);
    }

    public static BigDecimal round(BigDecimal a) {
        return a.setScale(SCALE, ROUNDING);
    }

    public static BigDecimal zero() {
        return BigDecimal.ZERO.setScale(SCALE, ROUNDING);
    }

    public static boolean isNegative(BigDecimal a) {
        return a.compareTo(BigDecimal.ZERO) < 0;
    }

    public static boolean isPositive(BigDecimal a) {
        return a.compareTo(BigDecimal.ZERO) > 0;
    }
}
