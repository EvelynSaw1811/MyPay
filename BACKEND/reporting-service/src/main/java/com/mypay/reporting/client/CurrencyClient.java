package com.mypay.reporting.client;

import com.mypay.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "CURRENCY-SERVICE", fallbackFactory = CurrencyClientFallbackFactory.class)
public interface CurrencyClient {

    @GetMapping("/api/currency/convert")
    ApiResponse<Map<String, Object>> convert(
            @RequestParam String from,
            @RequestParam String to,
            @RequestParam String amount);
}
