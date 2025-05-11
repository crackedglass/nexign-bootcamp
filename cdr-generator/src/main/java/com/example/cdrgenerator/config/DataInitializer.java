package com.example.cdrgenerator.config;

import com.example.cdrgenerator.entity.Subscriber;
import com.example.cdrgenerator.repository.SubscriberRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initSubscribers(SubscriberRepository subscriberRepository) {
        return args -> {
            List<String> msisdns = Arrays.asList(
                    "79001112233",
                    "79002223344",
                    "79003334455",
                    "79004445566",
                    "79005556677",
                    "79006667788",
                    "79007778899",
                    "79008889900",
                    "79009990011",
                    "79001234567",
                    "79007654321" // 11th subscriber
            );

            if (subscriberRepository.count() == 0) {
                msisdns.forEach(msisdn -> subscriberRepository.save(new Subscriber(msisdn)));
                System.out.println("Initialized " + msisdns.size() + " subscribers.");
            }
        };
    }
} 