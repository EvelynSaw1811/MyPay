package com.mypay.notification.mapper;

import com.mypay.notification.dto.UpdateUserPreferenceRequest;
import com.mypay.notification.dto.UserPreferenceResponse;
import com.mypay.notification.entity.UserPreference;
import org.springframework.stereotype.Component;

@Component
public class UserPreferenceMapper {

    public UserPreferenceResponse toResponse(UserPreference pref) {
        return UserPreferenceResponse.builder()
                .userPreferenceId(pref.getUserPreferenceId())
                .userId(pref.getUserPreferenceUserId())
                .emailEnabled(pref.isUserPreferenceEmailEnabled())
                .smsEnabled(pref.isUserPreferenceSmsEnabled())
                .pushEnabled(pref.isUserPreferencePushEnabled())
                .promoEnabled(pref.isUserPreferencePromoEnabled())
                .createdAt(pref.getUserPreferenceCreated())
                .updatedAt(pref.getUserPreferenceUpdated())
                .build();
    }

    /**
     * Applies non-null fields from the update request onto the existing entity.
     * Null fields are left untouched so the UI can patch a single toggle.
     */
    public void applyUpdate(UserPreference pref, UpdateUserPreferenceRequest request) {
        if (request.getEmailEnabled() != null) {
            pref.setUserPreferenceEmailEnabled(request.getEmailEnabled());
        }
        if (request.getSmsEnabled() != null) {
            pref.setUserPreferenceSmsEnabled(request.getSmsEnabled());
        }
        if (request.getPushEnabled() != null) {
            pref.setUserPreferencePushEnabled(request.getPushEnabled());
        }
        if (request.getPromoEnabled() != null) {
            pref.setUserPreferencePromoEnabled(request.getPromoEnabled());
        }
    }
}
