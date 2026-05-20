package com.mypay.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Archive record for a deleted MyPay user.
 * Rows are inserted before the corresponding USER_T row is removed.
 */
@Entity
@Table(name = "USER_LEGACY_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserLegacy {

    @Id
    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private String userId;

    @Column(name = "user_email", length = 255)
    private String userEmail;

    @Column(name = "user_phone", length = 20)
    private String userPhone;

    @Column(name = "user_fname", length = 100)
    private String userFirstName;

    @Column(name = "user_lname", length = 100)
    private String userLastName;

    @Column(name = "user_nickname", length = 100)
    private String userNickname;

    @Column(name = "user_invitation_code", length = 32)
    private String userInvitationCode;

    @Column(name = "user_status", length = 20)
    private String userStatus;

    @Column(name = "user_last_login")
    private LocalDateTime userLastLogin;

    @Column(name = "user_created")
    private LocalDateTime userCreated;

    @Column(name = "user_updated")
    private LocalDateTime userUpdated;

    @Column(name = "user_deleted")
    private LocalDateTime userDeleted;
}
