package com.sri.apisentinel.commonmodel;

import java.util.UUID;

public record ContractChange(
        UUID id,
        UUID contractId,
        String fromVersion,
        String toVersion,
        ChangeSeverity severity,
        boolean breaking,
        String path,
        String operation,
        String changeType,
        String description
) {
    public static ContractChange of(UUID contractId, String path, String operation,
                                    String changeType, ChangeSeverity severity,
                                    boolean breaking, String description) {
        return new ContractChange(UUID.randomUUID(), contractId, null, null,
                severity, breaking, path, operation, changeType, description);
    }
}