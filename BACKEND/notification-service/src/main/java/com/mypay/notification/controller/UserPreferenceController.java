package com.mypay.notification.controller;

import com.mypay.common.dto.ApiResponse;
import com.mypay.common.context.RequestContextHolder;
import com.mypay.notification.dto.UpdateUserPreferenceRequest;
import com.mypay.notification.dto.UserPreferenceResponse;
import com.mypay.notification.service.UserPreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications/preferences")
@RequiredArgsConstructor
public class UserPreferenceController {

    private final UserPreferenceService preferenceService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> get() {
        return ResponseEntity.ok(ApiResponse.success(preferenceService.getOrCreate(RequestContextHolder.currentUserId())));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserPreferenceResponse>> update(
            @Valid @RequestBody UpdateUserPreferenceRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Preferences updated", preferenceService.update(RequestContextHolder.currentUserId(), request)));
    }
}
