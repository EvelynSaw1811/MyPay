package com.mypay.reporting.client;

import com.mypay.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

@FeignClient(name = "COLLECTION-SERVICE", fallbackFactory = CollectionClientFallbackFactory.class)
public interface CollectionClient {

    @GetMapping("/api/collections")
    ApiResponse<List<Map<String, Object>>> getMyCollections(@RequestHeader("X-User-Id") String userId);

    @GetMapping("/api/collections/{collectionId}")
    ApiResponse<Map<String, Object>> getCollection(
            @PathVariable String collectionId,
            @RequestHeader("X-User-Id") String userId);

    @GetMapping("/api/collections/{collectionId}/expenses")
    ApiResponse<List<Map<String, Object>>> getExpenses(
            @PathVariable String collectionId,
            @RequestHeader("X-User-Id") String userId);

    @GetMapping("/api/collections/{collectionId}/balances")
    ApiResponse<List<Map<String, Object>>> getBalances(
            @PathVariable String collectionId,
            @RequestHeader("X-User-Id") String userId);
}
