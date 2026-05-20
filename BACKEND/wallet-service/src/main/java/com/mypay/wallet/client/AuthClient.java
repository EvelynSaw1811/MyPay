package com.mypay.wallet.client;

import com.mypay.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "AUTH-SERVICE")
public interface AuthClient {
    @GetMapping("/api/auth/internal/users/resolve")
    ApiResponse<Map<String, Object>> resolveUser(@RequestParam String identifier);
}
