package com.mypay.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Per-user notification channel preferences.
 *
 * One row per user, lazily created with safe defaults the first time the user
 * fetches their preferences. Owned by notification-service to keep the
 * downstream delivery logic colocated with the data that drives it.
 */
@Entity
@Table(name = "USER_PREFERENCE_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "uprf_id", columnDefinition = "CHAR(36)", updatable = false)
    private String userPreferenceId;

    @Column(name = "uprf_user_id", columnDefinition = "CHAR(36)", nullable = false, unique = true)
    private String userPreferenceUserId;

    @Column(name = "uprf_email_enabled", nullable = false)
    @Builder.Default
    private boolean userPreferenceEmailEnabled = true;

    @Column(name = "uprf_sms_enabled", nullable = false)
    @Builder.Default
    private boolean userPreferenceSmsEnabled = false;

    @Column(name = "uprf_push_enabled", nullable = false)
    @Builder.Default
    private boolean userPreferencePushEnabled = true;

    @Column(name = "uprf_promo_enabled", nullable = false)
    @Builder.Default
    private boolean userPreferencePromoEnabled = false;

    @Column(name = "uprf_created", updatable = false)
    private LocalDateTime userPreferenceCreated;

    @Column(name = "uprf_updated")
    private LocalDateTime userPreferenceUpdated;

    @PrePersist
    protected void onCreate() {
        userPreferenceCreated = LocalDateTime.now();
        userPreferenceUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        userPreferenceUpdated = LocalDateTime.now();
    }
}
