package com.example.hrsservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
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

    // Consuming CallDataDto from BRT
    @Value("${rabbitmq.queue.hrs_rating_request.name:hrs_rating_queue}")
    private String ratingRequestQueueName;

    @Value("${rabbitmq.exchange.hrs.name:hrs_exchange}")
    private String hrsExchangeName;

    @Value("${rabbitmq.routing.key.hrs_rating.name:hrs_rating_key}") // Key BRT uses to send TO HRS
    private String ratingRequestRoutingKey;

    // Sending RatingResponseDto to BRT
    // hrsExchangeName is reused for responses
    @Value("${rabbitmq.routing.key.hrs_response.name:hrs_response_key}") // Key HRS uses to send responses TO BRT
    private String ratingResponseRoutingKey;

    @Bean
    public Queue ratingRequestQueue() {
        return new Queue(ratingRequestQueueName, true);
    }

    @Bean
    public DirectExchange hrsExchange() {
        // This exchange is used for both BRT->HRS requests and HRS->BRT responses,
        // differentiated by routing keys.
        return new DirectExchange(hrsExchangeName);
    }

    @Bean
    public Binding ratingRequestBinding(Queue ratingRequestQueue, DirectExchange hrsExchange) {
        return BindingBuilder.bind(ratingRequestQueue).to(hrsExchange).with(ratingRequestRoutingKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // For LocalDateTime
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    @Primary
    public RabbitTemplate rabbitTemplate(final ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // Listeners for ratingRequestQueue will be annotated with @RabbitListener
    // in a service/component, and Spring AMQP will use the primary RabbitTemplate
    // and MessageConverter defined above.
} 