package com.example.cdrgenerator.impl.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.cdr.name:cdr_queue}")
    private String cdrQueueName;

    @Value("${rabbitmq.exchange.cdr.name:cdr_exchange}")
    private String cdrExchangeName;

    @Value("${rabbitmq.routing.key.cdr.name:cdr_key}")
    private String cdrRoutingKey;

    @Bean
    public Queue cdrQueue() {
        return QueueBuilder.durable(cdrQueueName)
                .build();
    }

    @Bean
    public DirectExchange cdrExchange() {
        return new DirectExchange(cdrExchangeName);
    }

    @Bean
    public Binding cdrBinding() {
        return BindingBuilder.bind(cdrQueue())
                .to(cdrExchange())
                .with(cdrRoutingKey);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }
}