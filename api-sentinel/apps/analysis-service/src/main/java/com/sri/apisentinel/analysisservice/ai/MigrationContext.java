package com.sri.apisentinel.analysisservice.ai;

import com.sri.apisentinel.commonmodel.ContractChange;

import java.util.List;
import java.util.UUID;

public record MigrationContext(
        UUID contractId,
        String apiName,
        String oldVersion,
        String newVersion,
        List<ContractChange> changes,
        List<String> consumers,
        int riskScore
) {}