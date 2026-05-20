package com.mypay.collection.messaging;

import com.mypay.common.event.NotificationEvent;
import com.mypay.common.event.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishExpenseCreated(NotificationEvent event) {
        publish(event, RabbitMQConstants.ROUTING_EXPENSE_CREATED);
    }

    public void publishInvitationReceived(NotificationEvent event) {
        publish(event, RabbitMQConstants.ROUTING_INVITATION_RECEIVED);
    }

    private void publish(NotificationEvent event, String routingKey) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConstants.EXCHANGE, routingKey, event);
        } catch (Exception e) {
            log.warn("Failed to publish notification event [{}]: {}", routingKey, e.getMessage());
        }
    }
}
