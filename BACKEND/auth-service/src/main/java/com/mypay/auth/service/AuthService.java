package com.mypay.auth.service;

import com.mypay.auth.dto.AuthResponse;
import com.mypay.auth.dto.ChangePasswordRequest;
import com.mypay.auth.dto.DeleteAccountRequest;
import com.mypay.auth.dto.LoginRequest;
import com.mypay.auth.dto.LogoutRequest;
import com.mypay.auth.dto.RefreshTokenRequest;
import com.mypay.auth.dto.RegisterRequest;
import com.mypay.auth.dto.UpdateUserRequest;
import com.mypay.auth.dto.UserResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(LogoutRequest request);
    UserResponse getUserById(String userId);
    UserResponse resolveUser(String identifier);

    /**
     * Updates an authenticated user's editable profile fields.
     *
     * @param userId      the target user id (from the request path)
     * @param request     the editable fields to apply
     * @param authUserId  the authenticated user id (from X-User-Id header) — must match userId
     */
    UserResponse updateUser(String userId, UpdateUserRequest request, String authUserId);

    /**
     * Changes the authenticated user's password after verifying the current one.
     *
     * @param userId      the target user id (from the request path)
     * @param request     DTO containing currentPassword and newPassword
     * @param authUserId  the authenticated user id — must match userId
     */
    void changePassword(String userId, ChangePasswordRequest request, String authUserId);

    /**
     * Permanently deletes the user account after password verification.
     * The user record and credentials are archived to legacy tables before deletion.
     *
     * @param userId      the target user id (from the request path)
     * @param request     DTO containing the password for confirmation
     * @param authUserId  the authenticated user id — must match userId
     */
    void deleteAccount(String userId, DeleteAccountRequest request, String authUserId);
}
