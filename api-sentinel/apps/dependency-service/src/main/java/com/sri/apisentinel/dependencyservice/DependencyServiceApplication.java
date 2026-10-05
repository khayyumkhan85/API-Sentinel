package com.sri.apisentinel.dependencyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
@EnableCaching
public class DependencyServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(DependencyServiceApplication.class, args);
    }
}