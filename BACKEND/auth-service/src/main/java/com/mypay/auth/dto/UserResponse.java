package com.mypay.auth.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String userNickname;
    private String invitationCode;
    private String phone;
    private String status;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
