package com.sri.apisentinel.commonmodel;

import java.util.List;
import java.util.UUID;

public record MigrationPlan(
        UUID id,
        UUID contractId,
        String assessment,
        String rationale,
        List<String> steps,
        String recommendedVersion,
        double confidence
) {
    public static MigrationPlan of(UUID contractId, String assessment, String rationale,
                                   List<String> steps, String recommendedVersion, double confidence) {
        return new MigrationPlan(UUID.randomUUID(), contractId, assessment, rationale,
                steps, recommendedVersion, confidence);
    }
}