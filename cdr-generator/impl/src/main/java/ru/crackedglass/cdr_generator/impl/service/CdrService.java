package ru.crackedglass.cdr_generator.impl.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class CdrService {

    private final GeneratorService generatorService;
    private final RabbitTemplate rabbitTemplate;
}
