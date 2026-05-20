package com.mypay.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private String notificationId;
    private String userId;
    private String type;
    private String title;
    private String message;
    private String referenceId;
    private boolean read;
    private LocalDateTime readAt;
    private LocalDateTime createdAt;
}
