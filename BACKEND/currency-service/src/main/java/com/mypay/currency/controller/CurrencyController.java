package com.mypay.currency.controller;

import com.mypay.common.dto.ApiResponse;
import com.mypay.currency.dto.ConversionResponse;
import com.mypay.currency.dto.CurrencyResponse;
import com.mypay.currency.dto.ExchangeRateResponse;
import com.mypay.currency.dto.ExchangeRateTableResponse;
import com.mypay.currency.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @GetMapping("/currencies")
    public ResponseEntity<ApiResponse<List<CurrencyResponse>>> getSupportedCurrencies() {
        return ResponseEntity.ok(ApiResponse.success(currencyService.getSupportedCurrencies()));
    }

    @GetMapping("/rates")
    public ResponseEntity<ApiResponse<ExchangeRateTableResponse>> getAllRates(
            @RequestParam(defaultValue = "MYR") String base) {
        return ResponseEntity.ok(ApiResponse.success(currencyService.getAllRatesFrom(base.toUpperCase())));
    }

    @GetMapping("/rates/{from}/{to}")
    public ResponseEntity<ApiResponse<ExchangeRateResponse>> getRate(
            @PathVariable String from,
            @PathVariable String to) {
        return ResponseEntity.ok(ApiResponse.success(currencyService.getRate(from, to)));
    }

    @GetMapping("/convert")
    public ResponseEntity<ApiResponse<ConversionResponse>> convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(ApiResponse.success(currencyService.convert(from, to, amount)));
    }
}
