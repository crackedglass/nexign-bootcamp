package com.example.brtservice.impl.config;

import com.example.brtservice.impl.listener.CdrMessageListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.cdr.name:cdr_queue}")
    private String cdrQueueName;

    @Value("${rabbitmq.exchange.cdr.name:cdr_exchange}")
    private String cdrExchangeName;

    @Value("${rabbitmq.routing.key.cdr.name:cdr_key}")
    private String cdrRoutingKey;

    @Value("${rabbitmq.exchange.hrs.name:hrs_exchange}")
    private String hrsExchangeName;

    @Value("${rabbitmq.routing.key.hrs.name:hrs_rating_key}")
    private String hrsRatingRoutingKey;

    @Value("${rabbitmq.queue.hrs_response.name:hrs_response_queue}")
    private String hrsResponseQueueName;

    @Value("${rabbitmq.routing.key.hrs_response.name:hrs_response_key}")
    private String hrsResponseRoutingKey;

    @Bean("cdrQueue")
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

    @Bean("cdrListenerContainer")
    public SimpleMessageListenerContainer cdrListenerContainer(ConnectionFactory connectionFactory,
                                                               @Value("${rabbitmq.queue.name:cdr_queue}") String queueName,
                                                               MessageListenerAdapter cdrListenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(cdrListenerAdapter);
        // CDRs are plain text, default String converter is fine.
        return container;
    }

    @Bean("cdrListenerAdapter")
    public MessageListenerAdapter cdrListenerAdapter(CdrMessageListener receiver) {
        return new MessageListenerAdapter(receiver, "receiveCdrFile");
    }

    @Bean
    public DirectExchange hrsExchange() {
        return new DirectExchange(hrsExchangeName);
    }

    @Bean
    public Queue hrsResponseQueue() {
        return QueueBuilder.durable(hrsResponseQueueName)
                .build();
    }

    @Bean
    public Binding hrsResponseBinding() {
        return BindingBuilder.bind(hrsResponseQueue())
                .to(hrsExchange())
                .with(hrsResponseRoutingKey);
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