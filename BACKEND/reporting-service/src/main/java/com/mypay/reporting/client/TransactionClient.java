package com.mypay.reporting.client;

import com.mypay.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

@FeignClient(name = "TRANSACTION-SERVICE", fallbackFactory = TransactionClientFallbackFactory.class)
public interface TransactionClient {

    @GetMapping("/api/transactions/history")
    ApiResponse<List<Map<String, Object>>> getHistory(@RequestHeader("X-User-Id") String userId);

    @GetMapping("/api/transactions/history/{currency}")
    ApiResponse<List<Map<String, Object>>> getHistoryByCurrency(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String currency);
}
