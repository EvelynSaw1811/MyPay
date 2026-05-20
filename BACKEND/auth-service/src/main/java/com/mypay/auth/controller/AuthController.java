package com.mypay.auth.controller;

import com.mypay.auth.dto.*;
import com.mypay.auth.service.AuthService;
import com.mypay.common.context.RequestContextHolder;
import com.mypay.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Registration successful", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully", null));
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String userId) {
        UserResponse response = authService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        String authUserId = RequestContextHolder.currentUserId();
        UserResponse response = authService.updateUser(userId, request, authUserId);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", response));
    }

    @PutMapping("/users/{userId}/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable String userId,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userId, request, RequestContextHolder.currentUserId());
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @PathVariable String userId,
            @Valid @RequestBody DeleteAccountRequest request) {
        authService.deleteAccount(userId, request, RequestContextHolder.currentUserId());
        return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
    }

    @GetMapping("/internal/users/resolve")
    public ResponseEntity<ApiResponse<UserResponse>> resolveUser(@RequestParam String identifier) {
        UserResponse response = authService.resolveUser(identifier);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
