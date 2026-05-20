package com.mypay.reporting.client;

import com.mypay.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class WalletClientFallbackFactory implements FallbackFactory<WalletClient> {
    @Override
    public WalletClient create(Throwable cause) {
        return userId -> {
            log.warn("WalletClient fallback triggered: {}", cause.getMessage());
            return ApiResponse.success(null);
        };
    }
}
