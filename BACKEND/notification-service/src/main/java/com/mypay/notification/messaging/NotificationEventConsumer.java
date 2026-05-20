package com.mypay.notification.messaging;

import com.mypay.common.event.NotificationEvent;
import com.mypay.common.event.RabbitMQConstants;
import com.mypay.notification.dto.CreateNotificationRequest;
import com.mypay.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_SETTLEMENT)
    public void handleSettlement(NotificationEvent event) {
        try {
            saveNotification(event);
        } catch (Exception e) {
            log.error("Failed to handle settlement notification event: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_EXPENSE)
    public void handleExpense(NotificationEvent event) {
        try {
            saveNotification(event);
        } catch (Exception e) {
            log.error("Failed to handle expense notification event: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = RabbitMQConstants.QUEUE_INVITATION)
    public void handleInvitation(NotificationEvent event) {
        try {
            saveNotification(event);
        } catch (Exception e) {
            log.error("Failed to handle invitation notification event: {}", e.getMessage());
        }
    }

    private void saveNotification(NotificationEvent event) {
        CreateNotificationRequest request = new CreateNotificationRequest();
        request.setUserId(event.getUserId());
        request.setType(event.getType());
        request.setTitle(event.getTitle());
        request.setMessage(event.getMessage());
        request.setReferenceId(event.getReferenceId());
        notificationService.create(request);
    }
}
