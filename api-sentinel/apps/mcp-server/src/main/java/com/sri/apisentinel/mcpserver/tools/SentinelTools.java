package com.sri.apisentinel.mcpserver.tools;

import com.sri.apisentinel.commonmodel.ApiConsumer;
import com.sri.apisentinel.commonmodel.ChangeSeverity;
import com.sri.apisentinel.commonmodel.ContractChange;
import com.sri.apisentinel.commonmodel.ImpactAssessment;
import com.sri.apisentinel.commonmodel.MigrationPlan;
import com.sri.apisentinel.openapidiff.ContractDiffService;
import com.sri.apisentinel.riskengine.DependencyGraph;
import com.sri.apisentinel.riskengine.ImpactAnalyzer;
import com.sri.apisentinel.riskengine.MigrationPlanner;
import com.sri.apisentinel.riskengine.RiskScoreCalculator;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class SentinelTools {

    private final ContractDiffService diffService = new ContractDiffService();
    private final RiskScoreCalculator riskCalculator = new RiskScoreCalculator();
    private final ImpactAnalyzer impactAnalyzer = new ImpactAnalyzer();
    private final MigrationPlanner planner = new MigrationPlanner();
    private final DependencyGraph graph = new DependencyGraph();

    @McpTool(name = "analyzeApiChange",
            description = "Diff two OpenAPI specifications and classify the change severity")
    public List<ContractChange> analyzeApiChange(String oldSpec, String newSpec) {
        return diffService.diff(UUID.randomUUID(), oldSpec, newSpec);
    }

    @McpTool(name = "getRiskScore",
            description = "Compute 0-100 risk score for a list of contract changes against a consumer count")
    public int getRiskScore(List<ContractChange> changes, int consumerCount) {
        return riskCalculator.calculate(changes, consumerCount);
    }

    @McpTool(name = "findAffectedConsumers",
            description = "Return the set of services that depend on the given API name")
    public List<String> findAffectedConsumers(String apiName) {
        return graph.consumersOf(apiName).stream().toList();
    }

    @McpTool(name = "registerConsumer",
            description = "Record that a service consumes an API version")
    public ApiConsumer registerConsumer(String serviceName, String apiName, String apiVersion) {
        ApiConsumer c = ApiConsumer.of(serviceName, apiName, apiVersion);
        graph.register(c);
        return c;
    }

    @McpTool(name = "assessImpact",
            description = "Full impact assessment: risk score, affected consumers, business impact, recommendation")
    public ImpactAssessment assessImpact(List<ContractChange> changes, List<ApiConsumer> consumers) {
        return impactAnalyzer.analyze(changes, consumers);
    }

    @McpTool(name = "createMigrationPlan",
            description = "Generate a step-by-step migration plan given an API change and its consumers")
    public MigrationPlan createMigrationPlan(UUID contractId, String apiName, String currentVersion,
                                             List<ContractChange> changes, List<String> consumers) {
        return planner.plan(contractId, apiName, currentVersion, changes, consumers);
    }

    @McpTool(name = "classifySeverity",
            description = "Return the highest severity across a set of contract changes")
    public ChangeSeverity classifySeverity(List<ContractChange> changes) {
        return diffService.highestSeverity(changes);
    }
}