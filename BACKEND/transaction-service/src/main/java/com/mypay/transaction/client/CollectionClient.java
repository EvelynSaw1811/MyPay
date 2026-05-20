package com.mypay.transaction.client;

import com.mypay.common.dto.ApiResponse;
import com.mypay.transaction.dto.ShareResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;

@FeignClient(name = "COLLECTION-SERVICE", fallbackFactory = CollectionClientFallbackFactory.class)
public interface CollectionClient {

    @GetMapping("/api/collections/{collectionId}/expenses/{expenseId}/shares/{shareId}")
    ApiResponse<ShareResponse> getShare(
            @PathVariable String collectionId,
            @PathVariable String expenseId,
            @PathVariable String shareId,
            @RequestHeader("X-User-Id") String userId);

    @PutMapping("/api/collections/{collectionId}/expenses/{expenseId}/shares/{shareId}/settle")
    ApiResponse<Void> settleShare(
            @PathVariable String collectionId,
            @PathVariable String expenseId,
            @PathVariable String shareId,
            @RequestHeader("X-User-Id") String userId);

    @PutMapping("/api/collections/{collectionId}/expenses/{expenseId}/shares/{shareId}/unsettle")
    ApiResponse<Void> unsettleShare(
            @PathVariable String collectionId,
            @PathVariable String expenseId,
            @PathVariable String shareId,
            @RequestHeader("X-User-Id") String userId);

    @GetMapping("/api/collections/{collectionId}/expenses/{expenseId}")
    ApiResponse<Map<String, Object>> getExpense(
            @PathVariable String collectionId,
            @PathVariable String expenseId,
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
