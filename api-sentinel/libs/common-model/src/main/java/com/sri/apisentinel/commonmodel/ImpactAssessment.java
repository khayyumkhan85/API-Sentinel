package com.sri.apisentinel.commonmodel;

import java.util.List;
import java.util.UUID;

public record ImpactAssessment(
        UUID id,
        UUID changeId,
        int riskScore,
        int affectedConsumers,
        String businessImpact,
        String recommendation,
        List<String> affectedServices
) {
    public static ImpactAssessment of(UUID changeId, int riskScore, int affectedConsumers,
                                      String businessImpact, String recommendation,
                                      List<String> affectedServices) {
        return new ImpactAssessment(UUID.randomUUID(), changeId, riskScore, affectedConsumers,
                businessImpact, recommendation, affectedServices);
    }
}