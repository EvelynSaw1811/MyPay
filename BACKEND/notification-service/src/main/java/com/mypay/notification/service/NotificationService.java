package com.mypay.notification.service;

import com.mypay.notification.dto.CreateNotificationRequest;
import com.mypay.notification.dto.NotificationResponse;

import java.util.List;

public interface NotificationService {
    NotificationResponse create(CreateNotificationRequest request);
    List<NotificationResponse> getAllForUser(String userId);
    List<NotificationResponse> getUnreadForUser(String userId);
    long getUnreadCount(String userId);
    void markAsRead(String notificationId, String userId);
    void markAllAsRead(String userId);
}
