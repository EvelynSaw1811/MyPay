package com.mypay.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Archive record for credentials of a deleted MyPay user.
 * Rows are inserted before the corresponding USER_CREDENTIAL_T row is removed.
 */
@Entity
@Table(name = "USER_CREDENTIAL_LEGACY_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCredentialLegacy {

    @Id
    @Column(name = "ucrd_id", columnDefinition = "CHAR(36)")
    private String userCredentialId;

    @Column(name = "ucrd_user_id", columnDefinition = "CHAR(36)")
    private String userCredentialUserId;

    @Column(name = "ucrd_pwd_hash", length = 255)
    private String userCredentialPwdHash;

    @Column(name = "ucrd_created")
    private LocalDateTime userCredentialCreated;

    @Column(name = "ucrd_deleted")
    private LocalDateTime userCredentialDeleted;
}
