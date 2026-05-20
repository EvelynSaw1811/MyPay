package com.mypay.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "USER_CREDENTIAL_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "ucrd_id", columnDefinition = "CHAR(36)", updatable = false)
    private String userCredentialId;

    @Column(name = "ucrd_user_id", columnDefinition = "CHAR(36)", nullable = false, unique = true)
    private String userCredentialUserId;

    @Column(name = "ucrd_pwd_hash", length = 255, nullable = false)
    private String userCredentialPwdHash;

    @Column(name = "ucrd_created", updatable = false)
    private LocalDateTime userCredentialCreated;

    @PrePersist
    protected void onCreate() {
        userCredentialCreated = LocalDateTime.now();
    }
}
