package com.sri.apisentinel.dependencyservice.config;

import com.sri.apisentinel.riskengine.DependencyGraph;
import com.sri.apisentinel.riskengine.RiskScoreCalculator;
import com.sri.apisentinel.riskengine.ImpactAnalyzer;
import com.sri.apisentinel.riskengine.MigrationPlanner;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class ApplicationConfig {

    @Bean
    public DependencyGraph dependencyGraph() {
        return new DependencyGraph();
    }

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