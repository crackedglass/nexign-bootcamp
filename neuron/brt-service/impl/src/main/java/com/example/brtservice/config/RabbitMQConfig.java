package com.example.brtservice.config;

import com.example.brtservice.listener.CdrMessageListener;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
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

    // For consuming CDRs from cdr-generator
    @Value("${rabbitmq.queue.name:cdr_queue}")
    private String cdrQueueName;

    @Value("${rabbitmq.exchange.name:cdr_exchange}")
    private String cdrExchangeName;

    @Value("${rabbitmq.routing.key.name:cdr_routing_key}")
    private String cdrRoutingKey;

    // For sending rating requests to HRS
    @Value("${rabbitmq.exchange.hrs.name:hrs_exchange}")
    private String hrsExchangeName;
    // Routing key for HRS rating requests is used by CdrProcessingService directly

    // For consuming rating responses from HRS
    @Value("${rabbitmq.queue.hrs_response.name:hrs_response_queue}")
    private String hrsResponseQueueName;

    @Value("${rabbitmq.routing.key.hrs_response.name:hrs_response_key}")
    private String hrsResponseRoutingKey;

    // --- Beans for CDR consumption ---
    @Bean("cdrQueue") // Named to avoid conflict if other Queue beans exist
    public Queue cdrQueue() {
        return new Queue(cdrQueueName, true);
    }

    @Bean("cdrExchange")
    public DirectExchange cdrExchange() {
        return new DirectExchange(cdrExchangeName);
    }

    @Bean("cdrBinding")
    public Binding cdrBinding(Queue cdrQueue, DirectExchange cdrExchange) {
        return BindingBuilder.bind(cdrQueue).to(cdrExchange).with(cdrRoutingKey);
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

    // --- Beans for HRS communication ---
    @Bean("hrsExchange")
    public DirectExchange hrsExchange() {
        // This exchange will be used by BRT to send requests to HRS
        // and by HRS to send responses back to BRT (via different routing keys)
        return new DirectExchange(hrsExchangeName);
    }

    @Bean("hrsResponseQueue")
    public Queue hrsResponseQueue() {
        return new Queue(hrsResponseQueueName, true);
    }

    @Bean("hrsResponseBinding")
    public Binding hrsResponseBinding(Queue hrsResponseQueue, DirectExchange hrsExchange) {
        return BindingBuilder.bind(hrsResponseQueue).to(hrsExchange).with(hrsResponseRoutingKey);
    }

    // Message converter for sending/receiving JSON DTOs (CallDataDto, RatingResponseDto)
    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // For LocalDateTime serialization/deserialization
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    @Primary // Make this the default RabbitTemplate
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
    
    // The HrsResponseMessageListener uses @RabbitListener, so it doesn't need a specific listener container bean here.
    // Spring AMQP will automatically create the necessary infrastructure for @RabbitListener methods,
    // provided a ConnectionFactory and a MessageConverter (if not using default) are available.
} 