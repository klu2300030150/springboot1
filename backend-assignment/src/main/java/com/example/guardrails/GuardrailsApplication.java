package com.example.guardrails;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class GuardrailsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GuardrailsApplication.class, args);
    }
}
