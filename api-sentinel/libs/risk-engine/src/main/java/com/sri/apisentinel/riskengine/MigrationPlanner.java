package com.sri.apisentinel.riskengine;

import com.sri.apisentinel.commonmodel.ContractChange;
import com.sri.apisentinel.commonmodel.MigrationPlan;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MigrationPlanner {

    public MigrationPlan plan(UUID contractId, String apiName, String currentVersion,
                              List<ContractChange> changes, List<String> affectedServices) {
        boolean hasBreaking = changes.stream().anyMatch(ContractChange::breaking);
        String recommendedVersion = hasBreaking ? bumpMajor(currentVersion) : bumpMinor(currentVersion);
        String assessment = hasBreaking ? "BREAKING_CHANGE" : "COMPATIBLE_CHANGE";
        String rationale = buildRationale(changes);
        List<String> steps = buildSteps(apiName, currentVersion, recommendedVersion, affectedServices, hasBreaking);
        double confidence = hasBreaking ? 0.92 : 0.98;
        return MigrationPlan.of(contractId, assessment, rationale, steps, recommendedVersion, confidence);
    }

    private String bumpMajor(String version) {
        try {
            String[] parts = version.split("\\.");
            int major = Integer.parseInt(parts[0]) + 1;
            return major + ".0.0";
        } catch (RuntimeException e) {
            return "next-major";
        }
    }

    private String bumpMinor(String version) {
        try {
            String[] parts = version.split("\\.");
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) + 1 : 1;
            return parts[0] + "." + minor + ".0";
        } catch (RuntimeException e) {
            return "next-minor";
        }
    }

    private String buildRationale(List<ContractChange> changes) {
        long breaking = changes.stream().filter(ContractChange::breaking).count();
        if (breaking == 0) {
            return "All changes are additive. Existing consumers remain compatible.";
        }
        return "%d breaking change(s) detected. Introduce a new version instead of modifying the existing one."
                .formatted(breaking);
    }

    private List<String> buildSteps(String apiName, String currentVersion, String nextVersion,
                                    List<String> affectedServices, boolean breaking) {
        List<String> steps = new ArrayList<>();
        if (breaking) {
            steps.add("Keep %s %s available".formatted(apiName, currentVersion));
            steps.add("Publish %s %s with new contract".formatted(apiName, nextVersion));
            for (String svc : affectedServices) {
                steps.add("Migrate consumer: " + svc);
            }
            steps.add("Monitor adoption of %s".formatted(nextVersion));
            steps.add("Mark %s as deprecated after 90 days".formatted(currentVersion));
        } else {
            steps.add("Deploy %s %s - no consumer action required".formatted(apiName, nextVersion));
        }
        return steps;
    }
}