package com.example.hrsservice.impl.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.hrs.name:hrs_exchange}")
    private String hrsExchangeName;

    @Value("${rabbitmq.queue.hrs_rating.name:hrs_rating_queue}")
    private String hrsRatingQueueName;

    @Value("${rabbitmq.routing.key.hrs_rating.name:hrs_rating_key}")
    private String hrsRatingRoutingKey;

    @Value("${rabbitmq.routing.key.hrs_response.name:hrs_response_key}")
    private String hrsResponseRoutingKey;

    @Bean
    public DirectExchange hrsExchange() {
        return new DirectExchange(hrsExchangeName);
    }

    @Bean
    public Queue hrsRatingQueue() {
        return QueueBuilder.durable(hrsRatingQueueName)
                .build();
    }

    @Bean
    public Binding hrsRatingBinding() {
        return BindingBuilder.bind(hrsRatingQueue())
                .to(hrsExchange())
                .with(hrsRatingRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
} 