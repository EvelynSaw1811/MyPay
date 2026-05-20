package com.mypay.auth.mapper;

import com.mypay.auth.dto.RegisterRequest;
import com.mypay.auth.dto.UpdateUserRequest;
import com.mypay.auth.dto.UserResponse;
import com.mypay.auth.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User toEntity(RegisterRequest request) {
        return User.builder()
                .userEmail(request.getEmail())
                .userFirstName(request.getFirstName())
                .userLastName(request.getLastName())
                .userNickname(request.getUserNickname())
                .userPhone(request.getPhone())
                .build();
    }

    public UserResponse toResponse(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .email(user.getUserEmail())
                .firstName(user.getUserFirstName())
                .lastName(user.getUserLastName())
                .userNickname(user.getUserNickname())
                .invitationCode(user.getUserInvitationCode())
                .phone(user.getUserPhone())
                .status(user.getUserStatus())
                .lastLogin(user.getUserLastLogin())
                .createdAt(user.getUserCreated())
                .updatedAt(user.getUserUpdated())
                .build();
    }

    /**
     * Applies non-null/non-blank fields from the update request onto the existing user entity.
     * Caller is responsible for any cross-entity validation (e.g. email uniqueness).
     */
    public void applyUpdate(User user, UpdateUserRequest request) {
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setUserEmail(request.getEmail().trim());
        }
        if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
            user.setUserFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null && !request.getLastName().isBlank()) {
            user.setUserLastName(request.getLastName().trim());
        }
        if (request.getUserNickname() != null && !request.getUserNickname().isBlank()) {
            user.setUserNickname(request.getUserNickname().trim());
        }
        if (request.getPhone() != null) {
            // Allow clearing the phone with an empty string.
            user.setUserPhone(request.getPhone().isBlank() ? null : request.getPhone().trim());
        }
    }
}
