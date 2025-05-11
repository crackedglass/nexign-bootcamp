package com.example.cdrgenerator.impl.config;

import com.example.cdrgenerator.impl.entity.Subscriber;
import com.example.cdrgenerator.impl.repository.SubscriberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final SubscriberRepository subscriberRepository;

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            if (subscriberRepository.count() == 0) {
                List<Subscriber> subscribers = Arrays.asList(
                    createSubscriber("79001234567"),
                    createSubscriber("79001234568"),
                    createSubscriber("79001234569"),
                    createSubscriber("79001234570"),
                    createSubscriber("79001234571"),
                    createSubscriber("79001234572"),
                    createSubscriber("79001234573"),
                    createSubscriber("79001234574"),
                    createSubscriber("79001234575"),
                    createSubscriber("79001234576"),
                    createSubscriber("79007654321")
                );
                subscriberRepository.saveAll(subscribers);
            }
        };
    }

    private Subscriber createSubscriber(String msisdn) {
        Subscriber subscriber = new Subscriber();
        subscriber.setMsisdn(msisdn);
        subscriber.setTariffId(11L);
        subscriber.setActive(true);
        return subscriber;
    }
} 