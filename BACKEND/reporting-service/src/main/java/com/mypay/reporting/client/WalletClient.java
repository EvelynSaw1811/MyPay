package com.mypay.reporting.client;

import com.mypay.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.Map;

@FeignClient(name = "WALLET-SERVICE", fallbackFactory = WalletClientFallbackFactory.class)
public interface WalletClient {

    @GetMapping("/api/wallets/me")
    ApiResponse<Map<String, Object>> getMyWallet(@RequestHeader("X-User-Id") String userId);
}
