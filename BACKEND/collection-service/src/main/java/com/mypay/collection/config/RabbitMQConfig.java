package com.mypay.collection.config;

import com.mypay.common.event.RabbitMQConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(RabbitMQConstants.EXCHANGE, true, false);
    }

    @Bean
    public Queue expenseQueue() {
        return new Queue(RabbitMQConstants.QUEUE_EXPENSE, true);
    }

    @Bean
    public Queue invitationQueue() {
        return new Queue(RabbitMQConstants.QUEUE_INVITATION, true);
    }

    @Bean
    public Binding expenseBinding(Queue expenseQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(expenseQueue).to(notificationExchange).with(RabbitMQConstants.KEY_EXPENSE);
    }

    @Bean
    public Binding invitationBinding(Queue invitationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(invitationQueue).to(notificationExchange).with(RabbitMQConstants.KEY_INVITATION);
    }

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(RabbitMQConstants.USER_EXCHANGE, true, false);
    }

    @Bean
    public Queue userCollectionQueue() {
        return new Queue(RabbitMQConstants.QUEUE_USER_COLLECTION, true);
    }

    @Bean
    public Binding userCollectionBinding(Queue userCollectionQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userCollectionQueue).to(userExchange).with(RabbitMQConstants.KEY_USER_REGISTERED);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter converter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }
}
