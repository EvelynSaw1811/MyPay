package com.mypay.currency.service;

import com.mypay.currency.dto.ConversionResponse;
import com.mypay.currency.dto.CurrencyResponse;
import com.mypay.currency.dto.ExchangeRateResponse;
import com.mypay.currency.dto.ExchangeRateTableResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CurrencyService {
    List<CurrencyResponse> getSupportedCurrencies();
    ExchangeRateTableResponse getAllRatesFrom(String baseCurrency);
    ExchangeRateResponse getRate(String from, String to);
    ConversionResponse convert(String from, String to, BigDecimal amount);
}
