package com.mypay.wallet.messaging;

import com.mypay.common.event.RabbitMQConstants;
import com.mypay.common.event.UserRegisteredEvent;
import com.mypay.wallet.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final WalletService walletService;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_USER_WALLET)
    public void onUserRegistered(UserRegisteredEvent event) {
        String userId = event.getUserId();
        log.info("[Event] Received user.registered for userId={}", userId);
        try {
            walletService.createAccount(userId, event.getWalletCurrencies());
        } catch (com.mypay.common.exception.DuplicateResourceException e) {
            log.warn("[Event] Account already exists for userId={} - skipping", userId);
        }
    }
}
