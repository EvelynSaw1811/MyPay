package com.mypay.notification.dto;

import lombok.Data;

/**
 * Update DTO for notification preferences. All fields are optional —
 * only non-null values are applied so the UI can patch a single toggle.
 */
@Data
public class UpdateUserPreferenceRequest {
    private Boolean emailEnabled;
    private Boolean smsEnabled;
    private Boolean pushEnabled;
    private Boolean promoEnabled;
}
