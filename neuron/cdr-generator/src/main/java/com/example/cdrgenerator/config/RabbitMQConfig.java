package com.example.cdrgenerator.config;

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

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.name:cdr_queue}")
    private String queueName;

    @Value("${rabbitmq.exchange.name:cdr_exchange}")
    private String exchangeName;

    @Value("${rabbitmq.routing.key.name:cdr_routing_key}")
    private String routingKey;

    @Bean
    public Queue queue() {
        // Durable queue
        return new Queue(queueName, true);
    }

    @Bean
    public DirectExchange exchange() {
        // Direct exchange
        return new DirectExchange(exchangeName);
    }

    @Bean
    public Binding binding(Queue queue, DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(routingKey);
    }

    /**
     * Using StringHttpMessageConverter by default for text/plain if RabbitTemplate is used with String payload.
     * If sending objects, a Jackson2JsonMessageConverter is good.
     * Since CDRs are plain text, default converter for String should be fine.
     * If we wanted to send structured messages (e.g., JSON objects), we would configure Jackson2JsonMessageConverter.
     */
    // @Bean
    // public MessageConverter jsonMessageConverter() {
    //     return new Jackson2JsonMessageConverter();
    // }

    // @Bean
    // public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
    //     final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
    //     // rabbitTemplate.setMessageConverter(jsonMessageConverter()); // Uncomment if using JSON
    //     return rabbitTemplate;
    // }
} 