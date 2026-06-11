package com.mypay.notification.repository;

import com.mypay.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, String> {
    List<Notification> findByNotificationUserIdOrderByNotificationCreatedDesc(String userId);
    List<Notification> findByNotificationUserIdAndNotificationReadFalseOrderByNotificationCreatedDesc(String userId);
    long countByNotificationUserIdAndNotificationReadFalse(String userId);
    boolean existsByNotificationReferenceIdAndNotificationType(String referenceId, String type);
    Optional<Notification> findByNotificationReferenceIdAndNotificationType(String referenceId, String type);
}
