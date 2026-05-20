package com.mypay.transaction.client;

import com.mypay.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "WALLET-SERVICE", fallbackFactory = WalletClientFallbackFactory.class)
public interface WalletClient {

    @PostMapping("/api/wallets/internal/debit/{userId}")
    ApiResponse<Void> debit(@PathVariable String userId, @RequestBody Map<String, Object> request);

    @PostMapping("/api/wallets/internal/credit/{userId}")
    ApiResponse<Void> credit(@PathVariable String userId, @RequestBody Map<String, Object> request);
}
