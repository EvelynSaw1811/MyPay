package com.mypay.notification.service.impl;

import com.mypay.common.exception.ForbiddenException;
import com.mypay.common.exception.ResourceNotFoundException;
import com.mypay.notification.dto.CreateNotificationRequest;
import com.mypay.notification.dto.NotificationResponse;
import com.mypay.notification.entity.Notification;
import com.mypay.notification.repository.NotificationRepository;
import com.mypay.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification notification = Notification.builder()
                .notificationUserId(request.getUserId())
                .notificationType(request.getType())
                .notificationTitle(request.getTitle())
                .notificationMessage(request.getMessage())
                .notificationReferenceId(request.getReferenceId())
                .build();
        return toResponse(notificationRepository.save(notification));
    }

    @Override
    public List<NotificationResponse> getAllForUser(String userId) {
        return notificationRepository.findByNotificationUserIdOrderByNotificationCreatedDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public List<NotificationResponse> getUnreadForUser(String userId) {
        return notificationRepository.findByNotificationUserIdAndNotificationReadFalseOrderByNotificationCreatedDesc(userId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public long getUnreadCount(String userId) {
        return notificationRepository.countByNotificationUserIdAndNotificationReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        if (!notification.getNotificationUserId().equals(userId)) {
            throw new ForbiddenException("Access denied");
        }
        notification.setNotificationRead(true);
        notification.setNotificationReadDateTime(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(String userId) {
        List<Notification> unread = notificationRepository.findByNotificationUserIdAndNotificationReadFalseOrderByNotificationCreatedDesc(userId);
        LocalDateTime now = LocalDateTime.now();
        unread.forEach(n -> {
            n.setNotificationRead(true);
            n.setNotificationReadDateTime(now);
        });
        notificationRepository.saveAll(unread);
    }

    private NotificationResponse toResponse(Notification n) {
        return NotificationResponse.builder()
                .notificationId(n.getNotificationId())
                .userId(n.getNotificationUserId())
                .type(n.getNotificationType())
                .title(n.getNotificationTitle())
                .message(n.getNotificationMessage())
                .referenceId(n.getNotificationReferenceId())
                .read(n.isNotificationRead())
                .readAt(n.getNotificationReadDateTime())
                .createdAt(n.getNotificationCreated())
                .build();
    }
}
