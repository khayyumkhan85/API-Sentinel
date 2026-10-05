package com.sri.apisentinel.contractservice.config;

import com.sri.apisentinel.openapidiff.ContractDiffService;
import com.sri.apisentinel.openapidiff.OpenApiParser;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class ApplicationConfig {

    @Bean
    public OpenApiParser openApiParser() {
        return new OpenApiParser();
    }

    @Bean
    public ContractDiffService contractDiffService() {
        return new ContractDiffService();
    }
}