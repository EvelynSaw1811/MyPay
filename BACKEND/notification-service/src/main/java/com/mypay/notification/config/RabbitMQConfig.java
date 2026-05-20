package com.mypay.notification.config;

import com.mypay.common.event.RabbitMQConstants;
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
    public Queue settlementQueue() {
        return new Queue(RabbitMQConstants.QUEUE_SETTLEMENT, true);
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
    public Binding settlementBinding(TopicExchange notificationExchange) {
        return BindingBuilder.bind(settlementQueue()).to(notificationExchange).with(RabbitMQConstants.KEY_SETTLEMENT);
    }

    @Bean
    public Binding expenseBinding(TopicExchange notificationExchange) {
        return BindingBuilder.bind(expenseQueue()).to(notificationExchange).with(RabbitMQConstants.KEY_EXPENSE);
    }

    @Bean
    public Binding invitationBinding(TopicExchange notificationExchange) {
        return BindingBuilder.bind(invitationQueue()).to(notificationExchange).with(RabbitMQConstants.KEY_INVITATION);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
