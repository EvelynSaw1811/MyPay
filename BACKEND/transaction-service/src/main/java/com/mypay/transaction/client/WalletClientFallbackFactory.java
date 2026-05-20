package com.mypay.transaction.client;

import com.mypay.common.dto.ApiResponse;
import com.mypay.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WalletClientFallbackFactory implements FallbackFactory<WalletClient> {

    @Override
    public WalletClient create(Throwable cause) {
        return new WalletClient() {
            @Override
            public ApiResponse<Void> debit(String userId, java.util.Map<String, Object> request) {
                log.error("Wallet debit failed for userId={}: {}", userId, cause.getMessage());
                throw new BadRequestException("Wallet service unavailable for debit operation");
            }

            @Override
            public ApiResponse<Void> credit(String userId, java.util.Map<String, Object> request) {
                log.error("Wallet credit failed for userId={}: {}", userId, cause.getMessage());
                throw new BadRequestException("Wallet service unavailable for credit operation");
            }
        };
    }
}
