package com.mypay.transaction.client;

import com.mypay.common.dto.ApiResponse;
import com.mypay.common.exception.BadRequestException;
import com.mypay.transaction.dto.ShareResponse;
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
            public ApiResponse<ShareResponse> getShare(String c, String e, String s, String userId) {
                log.error("Collection service unavailable for getShare: {}", cause.getMessage());
                throw new BadRequestException("Collection service unavailable");
            }

            @Override
            public ApiResponse<Void> settleShare(String c, String e, String s, String userId) {
                log.error("Collection service unavailable for settleShare: {}", cause.getMessage());
                throw new BadRequestException("Collection service unavailable");
            }

            @Override
            public ApiResponse<Void> unsettleShare(String c, String e, String s, String userId) {
                log.error("Collection service unavailable for unsettleShare: {}", cause.getMessage());
                throw new BadRequestException("Collection service unavailable");
            }

            @Override
            public ApiResponse<Map<String, Object>> getExpense(String collectionId, String expenseId, String userId) {
                log.error("Collection service unavailable for getExpense: {}", cause.getMessage());
                throw new BadRequestException("Collection service unavailable");
            }

            @Override
            public ApiResponse<List<Map<String, Object>>> getExpenses(String collectionId, String userId) {
                log.warn("Collection service unavailable for getExpenses: {}", cause.getMessage());
                return ApiResponse.success(Collections.emptyList());
            }

            @Override
            public ApiResponse<List<Map<String, Object>>> getBalances(String collectionId, String userId) {
                log.warn("Collection service unavailable for getBalances: {}", cause.getMessage());
                return ApiResponse.success(Collections.emptyList());
            }
        };
    }
}
