package com.mypay.notification.controller;

import com.mypay.common.dto.ApiResponse;
import com.mypay.common.context.RequestContextHolder;
import com.mypay.notification.dto.CreateNotificationRequest;
import com.mypay.notification.dto.NotificationResponse;
import com.mypay.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/internal/create")
    public ResponseEntity<ApiResponse<NotificationResponse>> create(
            @Valid @RequestBody CreateNotificationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(notificationService.create(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getAllForUser(RequestContextHolder.currentUserId())));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getUnread() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getUnreadForUser(RequestContextHolder.currentUserId())));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount() {
        return ResponseEntity.ok(ApiResponse.success(Map.of("count", notificationService.getUnreadCount(RequestContextHolder.currentUserId()))));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @PathVariable String id) {
        notificationService.markAsRead(id, RequestContextHolder.currentUserId());
        return ResponseEntity.ok(ApiResponse.success("Notification marked as read", null));
    }

    @PutMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead() {
        notificationService.markAllAsRead(RequestContextHolder.currentUserId());
        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read", null));
    }
}
