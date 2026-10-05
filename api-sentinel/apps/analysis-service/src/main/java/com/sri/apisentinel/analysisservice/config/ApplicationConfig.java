package com.sri.apisentinel.analysisservice.config;

import com.sri.apisentinel.riskengine.ImpactAnalyzer;
import com.sri.apisentinel.riskengine.MigrationPlanner;
import com.sri.apisentinel.riskengine.RiskScoreCalculator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    public RiskScoreCalculator riskScoreCalculator() {
        return new RiskScoreCalculator();
    }

    @Bean
    public ImpactAnalyzer impactAnalyzer() {
        return new ImpactAnalyzer();
    }

    @Bean
    public MigrationPlanner migrationPlanner() {
        return new MigrationPlanner();
    }
}