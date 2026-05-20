package com.mypay.common.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CurrencyCode {
    MYR("Malaysian Ringgit", "MYR"),
    SGD("Singapore Dollar", "SGD"),
    USD("United States Dollar", "USD");

    private final String displayName;
    private final String code;

    public static final String DEFAULT_CODE = "MYR";
}
