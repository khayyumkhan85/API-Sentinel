package com.sri.apisentinel.analysisservice.service;

import com.sri.apisentinel.analysisservice.ai.AiMigrationAdvisor;
import com.sri.apisentinel.analysisservice.ai.MigrationContext;
import com.sri.apisentinel.commonmodel.ContractChange;
import com.sri.apisentinel.commonmodel.ImpactAssessment;
import com.sri.apisentinel.commonmodel.MigrationPlan;
import com.sri.apisentinel.commonmodel.ApiConsumer;
import com.sri.apisentinel.riskengine.ImpactAnalyzer;
import com.sri.apisentinel.riskengine.RiskScoreCalculator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class AnalysisService {

    private final RiskScoreCalculator riskCalculator;
    private final ImpactAnalyzer impactAnalyzer;
    private final AiMigrationAdvisor advisor;

    public AnalysisService(RiskScoreCalculator riskCalculator,
                           ImpactAnalyzer impactAnalyzer,
                           AiMigrationAdvisor advisor) {
        this.riskCalculator = riskCalculator;
        this.impactAnalyzer = impactAnalyzer;
        this.advisor = advisor;
    }

    public ImpactAssessment assess(List<ContractChange> changes, List<ApiConsumer> consumers) {
        return impactAnalyzer.analyze(changes, consumers);
    }

    public int risk(List<ContractChange> changes, int consumerCount) {
        return riskCalculator.calculate(changes, consumerCount);
    }

    public MigrationPlan migrationPlan(UUID contractId, String apiName, String oldVersion,
                                       List<ContractChange> changes, List<String> consumerNames) {
        int score = riskCalculator.calculate(changes, consumerNames.size());
        MigrationContext ctx = new MigrationContext(contractId, apiName, oldVersion, null,
                changes, consumerNames, score);
        return advisor.advise(ctx);
    }
}