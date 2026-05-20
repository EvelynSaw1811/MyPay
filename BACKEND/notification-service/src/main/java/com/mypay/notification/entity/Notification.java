package com.mypay.notification.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATION_T")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "notf_id", columnDefinition = "CHAR(36)", updatable = false)
    private String notificationId;

    @Column(name = "notf_user_id", columnDefinition = "CHAR(36)", nullable = false)
    private String notificationUserId;

    @Column(name = "notf_type", length = 50)
    private String notificationType;

    @Column(name = "notf_title", length = 255)
    private String notificationTitle;

    @Column(name = "notf_message", columnDefinition = "TEXT")
    private String notificationMessage;

    @Column(name = "notf_ref_id", columnDefinition = "CHAR(36)")
    private String notificationReferenceId;

    @Column(name = "notf_read")
    @Builder.Default
    private boolean notificationRead = false;

    @Column(name = "notf_read_at")
    private LocalDateTime notificationReadDateTime;

    @Column(name = "notf_created", updatable = false)
    private LocalDateTime notificationCreated;

    @PrePersist
    protected void onCreate() {
        notificationCreated = LocalDateTime.now();
    }
}
