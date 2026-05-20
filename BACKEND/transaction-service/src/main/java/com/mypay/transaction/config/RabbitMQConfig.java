package com.mypay.transaction.config;

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
    public Binding settlementBinding(Queue settlementQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(settlementQueue).to(notificationExchange).with(RabbitMQConstants.KEY_SETTLEMENT);
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
