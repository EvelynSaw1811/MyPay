package com.mypay.reporting.client;

import com.mypay.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class CurrencyClientFallbackFactory implements FallbackFactory<CurrencyClient> {
    @Override
    public CurrencyClient create(Throwable cause) {
        return (from, to, amount) -> {
            log.warn("CurrencyClient convert fallback: {}", cause.getMessage());
            return ApiResponse.success(null);
        };
    }
}
