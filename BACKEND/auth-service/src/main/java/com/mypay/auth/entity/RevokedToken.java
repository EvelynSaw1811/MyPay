package com.mypay.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "REVOKED_TOKEN_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RevokedToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "rtkn_id", columnDefinition = "CHAR(36)", updatable = false)
    private String revokedToken;

    @Column(name = "rtkn_token_hash", length = 64, nullable = false, unique = true)
    private String revokedTokenHash;

    @Column(name = "rtkn_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String revokedTokenUserId;

    @Column(name = "rtkn_expires_at", nullable = false)
    private LocalDateTime revokedTokenExpireDateTime;

    @Column(name = "rtkn_revoked_at", updatable = false)
    private LocalDateTime revokedTokenRevokeDateTime;

    @PrePersist
    protected void onCreate() {
        revokedTokenRevokeDateTime = LocalDateTime.now();
    }
}
