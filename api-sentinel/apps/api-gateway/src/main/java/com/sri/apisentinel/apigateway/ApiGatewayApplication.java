package com.sri.apisentinel.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator routes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("contract-service", r -> r.path("/api/v1/contracts/**")
                        .uri("http://contract-service:8081"))
                .route("dependency-service", r -> r.path("/api/v1/consumers/**")
                        .uri("http://dependency-service:8082"))
                .route("analysis-service", r -> r.path("/api/v1/analysis/**")
                        .uri("http://analysis-service:8083"))
                .route("mcp-server", r -> r.path("/mcp/**")
                        .uri("http://mcp-server:8084"))
                .build();
    }
}