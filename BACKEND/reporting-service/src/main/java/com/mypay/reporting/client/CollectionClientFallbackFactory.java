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
public class CollectionClientFallbackFactory implements FallbackFactory<CollectionClient> {
    @Override
    public CollectionClient create(Throwable cause) {
        return new CollectionClient() {
            @Override
            public ApiResponse<List<Map<String, Object>>> getMyCollections(String userId) {
                log.warn("CollectionClient getMyCollections fallback: {}", cause.getMessage());
                return ApiResponse.success(Collections.emptyList());
            }

            @Override
            public ApiResponse<Map<String, Object>> getCollection(String collectionId, String userId) {
                log.warn("CollectionClient getCollection fallback: {}", cause.getMessage());
                return ApiResponse.success(Collections.emptyMap());
            }

            @Override
            public ApiResponse<List<Map<String, Object>>> getExpenses(String collectionId, String userId) {
                log.warn("CollectionClient getExpenses fallback: {}", cause.getMessage());
                return ApiResponse.success(Collections.emptyList());
            }

            @Override
            public ApiResponse<List<Map<String, Object>>> getBalances(String collectionId, String userId) {
                log.warn("CollectionClient getBalances fallback: {}", cause.getMessage());
                return ApiResponse.success(Collections.emptyList());
            }
        };
    }
}
