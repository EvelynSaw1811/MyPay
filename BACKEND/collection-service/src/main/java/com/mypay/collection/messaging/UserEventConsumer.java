package com.mypay.collection.messaging;

import com.mypay.collection.entity.CollectionType;
import com.mypay.collection.repository.CollectionTypeRepository;
import com.mypay.common.event.RabbitMQConstants;
import com.mypay.common.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private static final List<String> DEFAULT_TYPES = List.of("Expense", "Trip", "Monthly", "Other");

    private final CollectionTypeRepository collectionTypeRepository;

    @RabbitListener(queues = RabbitMQConstants.QUEUE_USER_COLLECTION)
    public void onUserRegistered(UserRegisteredEvent event) {
        String userId = event.getUserId();
        log.info("[Event] Received user.registered for collection defaults userId={}", userId);
        for (String name : DEFAULT_TYPES) {
            if (!collectionTypeRepository.existsByCollectionTypeUserIdAndCollectionTypeNameIgnoreCase(userId, name)) {
                collectionTypeRepository.save(CollectionType.builder()
                        .collectionTypeUserId(userId)
                        .collectionTypeName(name)
                        .collectionTypeSystem(true)
                        .build());
            }
        }
    }
}
