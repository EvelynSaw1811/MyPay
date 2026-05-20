package com.mypay.notification.service;

import com.mypay.notification.dto.UpdateUserPreferenceRequest;
import com.mypay.notification.dto.UserPreferenceResponse;

public interface UserPreferenceService {

    /**
     * Returns the user's preferences, lazily creating a row with default
     * values the first time a user requests them.
     */
    UserPreferenceResponse getOrCreate(String userId);

    /** Applies non-null fields from the request onto the user's preferences. */
    UserPreferenceResponse update(String userId, UpdateUserPreferenceRequest request);
}
