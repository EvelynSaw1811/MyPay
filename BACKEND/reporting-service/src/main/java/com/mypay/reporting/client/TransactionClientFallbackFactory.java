package com.mypay.reporting.client;

import com.mypay.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class TransactionClientFallbackFactory implements FallbackFactory<TransactionClient> {
    @Override
    public TransactionClient create(Throwable cause) {
        return new TransactionClient() {
            @Override
            public ApiResponse<List<Map<String, Object>>> getHistory(String userId) {
                log.warn("TransactionClient getHistory fallback: {}", cause.getMessage());
                return ApiResponse.success(Collections.emptyList());
            }

            @Override
            public ApiResponse<List<Map<String, Object>>> getHistoryByCurrency(String userId, String currency) {
                log.warn("TransactionClient getHistoryByCurrency fallback: {}", cause.getMessage());
                return ApiResponse.success(Collections.emptyList());
            }
        };
    }
}
