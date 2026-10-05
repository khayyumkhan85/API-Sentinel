package com.sri.apisentinel.riskengine;

import com.sri.apisentinel.commonmodel.ApiConsumer;
import com.sri.apisentinel.commonmodel.ContractChange;
import com.sri.apisentinel.commonmodel.ImpactAssessment;

import java.util.List;

public class ImpactAnalyzer {

    private final RiskScoreCalculator riskCalculator = new RiskScoreCalculator();

    public ImpactAssessment analyze(List<ContractChange> changes, List<ApiConsumer> consumers) {
        int score = riskCalculator.calculate(changes, consumers.size());
        String businessImpact = summarizeBusinessImpact(changes, consumers);
        String recommendation = recommend(score, hasBreaking(changes));
        List<String> services = consumers.stream().map(ApiConsumer::serviceName).distinct().toList();
        var contractId = changes.isEmpty() ? null : changes.get(0).contractId();
        return ImpactAssessment.of(contractId, score, consumers.size(),
                businessImpact, recommendation, services);
    }

    private boolean hasBreaking(List<ContractChange> changes) {
        return changes.stream().anyMatch(ContractChange::breaking);
    }

    private String summarizeBusinessImpact(List<ContractChange> changes, List<ApiConsumer> consumers) {
        long breakingCount = changes.stream().filter(ContractChange::breaking).count();
        if (breakingCount == 0) {
            return "No breaking changes detected. Safe to roll out.";
        }
        return "%d breaking change(s) impacting %d consumer(s). May cause deserialization or request failures."
                .formatted(breakingCount, consumers.size());
    }

    private String recommend(int score, boolean breaking) {
        if (!breaking) return "APPROVE";
        if (score >= 80) return "BLOCK - introduce new major version";
        if (score >= 50) return "REVIEW - coordinate migration with consumers";
        return "APPROVE WITH CAUTION - deprecate removed fields";
    }
}