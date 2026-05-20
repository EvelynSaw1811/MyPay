package com.mypay.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "USER_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", columnDefinition = "CHAR(36)", updatable = false)
    private String userId;

    @Column(name = "user_email", length = 255, nullable = false, unique = true)
    private String userEmail;

    @Column(name = "user_phone", length = 20)
    private String userPhone;

    @Column(name = "user_fname", length = 100, nullable = false)
    private String userFirstName;

    @Column(name = "user_lname", length = 100, nullable = false)
    private String userLastName;

    @Column(name = "user_nickname", length = 100)
    private String userNickname;

    @Column(name = "user_invitation_code", length = 32, unique = true)
    private String userInvitationCode;

    @Column(name = "user_status", length = 20)
    @Builder.Default
    private String userStatus = "ACTIVE";

    @Column(name = "user_last_login")
    private LocalDateTime userLastLogin;

    @Column(name = "user_created", updatable = false)
    private LocalDateTime userCreated;

    @Column(name = "user_updated")
    private LocalDateTime userUpdated;

    @PrePersist
    protected void onCreate() {
        userCreated = LocalDateTime.now();
        userUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        userUpdated = LocalDateTime.now();
    }
}
