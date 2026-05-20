package com.mypay.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateNotificationRequest {
    @NotBlank
    private String userId;
    @NotBlank
    private String type;
    @NotBlank
    private String title;
    private String message;
    private String referenceId;
}
