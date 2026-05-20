package com.mypay.auth.messaging;

import com.mypay.common.event.RabbitMQConstants;
import com.mypay.common.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    public void publishUserRegistered(String userId, String userNickname, Set<String> walletCurrencies) {
        UserRegisteredEvent event = UserRegisteredEvent.builder()
                .userId(userId)
                .userNickname(userNickname)
                .walletCurrencies(walletCurrencies)
                .build();
        rabbitTemplate.convertAndSend(
                RabbitMQConstants.USER_EXCHANGE,
                RabbitMQConstants.KEY_USER_REGISTERED,
                event);
        log.debug("[Event] Published user.registered for userId={} currencies={}", userId, walletCurrencies);
    }
}
