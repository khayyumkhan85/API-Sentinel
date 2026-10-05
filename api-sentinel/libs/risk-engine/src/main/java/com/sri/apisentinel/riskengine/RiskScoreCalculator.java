package com.sri.apisentinel.riskengine;

import com.sri.apisentinel.commonmodel.ChangeSeverity;
import com.sri.apisentinel.commonmodel.ContractChange;

import java.util.List;

public class RiskScoreCalculator {

    public int calculate(List<ContractChange> changes, int affectedConsumers) {
        if (changes == null || changes.isEmpty()) {
            return 0;
        }
        int base = 0;
        for (ContractChange c : changes) {
            base += severityWeight(c.severity());
            if (c.breaking()) {
                base += 10;
            }
        }
        int consumerMultiplier = Math.min(affectedConsumers * 3, 30);
        return Math.min(100, base + consumerMultiplier);
    }

    private int severityWeight(ChangeSeverity severity) {
        return switch (severity) {
            case NONE -> 0;
            case LOW -> 2;
            case MEDIUM -> 8;
            case HIGH -> 18;
            case CRITICAL -> 35;
        };
    }

    public RiskBand band(int score) {
        if (score >= 80) return RiskBand.CRITICAL;
        if (score >= 50) return RiskBand.HIGH;
        if (score >= 25) return RiskBand.MEDIUM;
        if (score > 0) return RiskBand.LOW;
        return RiskBand.NONE;
    }

    public enum RiskBand {
        NONE, LOW, MEDIUM, HIGH, CRITICAL
    }
}