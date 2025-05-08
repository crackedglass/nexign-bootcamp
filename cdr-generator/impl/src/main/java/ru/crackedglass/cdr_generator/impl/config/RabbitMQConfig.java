package ru.crackedglass.cdr_generator.impl.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        return new RabbitTemplate(connectionFactory);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("cdr-exchange");
    }

    @Bean
    Queue queue() {
        return new Queue("cdr-queue");
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory, Queue queue) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queue.getName());
        return container;
    }
}
