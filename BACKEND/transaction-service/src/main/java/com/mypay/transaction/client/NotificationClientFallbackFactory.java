package com.mypay.transaction.client;

import com.mypay.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificationClientFallbackFactory implements FallbackFactory<NotificationClient> {

    @Override
    public NotificationClient create(Throwable cause) {
        return request -> {
            log.warn("Notification service unavailable: {}", cause.getMessage());
            return ApiResponse.success("Notification deferred", null);
        };
    }
}
