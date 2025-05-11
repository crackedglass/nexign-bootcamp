package com.example.cdrgenerator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // To enable scheduled tasks for CDR generation
public class CdrGeneratorApplication {

    public static void main(String[] args) {
        SpringApplication.run(CdrGeneratorApplication.class, args);
    }

} 