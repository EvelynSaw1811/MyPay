package com.mypay.notification.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserPreferenceResponse {
    private String userPreferenceId;
    private String userId;
    private boolean emailEnabled;
    private boolean smsEnabled;
    private boolean pushEnabled;
    private boolean promoEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
