package com.mypay.wallet.config;

import com.mypay.common.event.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(RabbitMQConstants.USER_EXCHANGE);
    }

    @Bean
    public Queue userWalletQueue() {
        return new Queue(RabbitMQConstants.QUEUE_USER_WALLET, true);
    }

    @Bean
    public Binding userWalletBinding(Queue userWalletQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userWalletQueue)
                .to(userExchange)
                .with(RabbitMQConstants.KEY_USER_REGISTERED);
    }
}
