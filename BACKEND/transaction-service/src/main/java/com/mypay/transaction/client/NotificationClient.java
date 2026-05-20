package com.mypay.transaction.client;

import com.mypay.common.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "NOTIFICATION-SERVICE", fallbackFactory = NotificationClientFallbackFactory.class)
public interface NotificationClient {

    @PostMapping("/api/notifications/internal/create")
    ApiResponse<Void> createNotification(@RequestBody Map<String, String> request);
}
