package com.sri.apisentinel.analysisservice.ai;

import com.sri.apisentinel.commonmodel.ContractChange;
import com.sri.apisentinel.commonmodel.MigrationPlan;
import com.sri.apisentinel.riskengine.MigrationPlanner;
import org.springframework.stereotype.Component;

/**
 * Facade over the deterministic MigrationPlanner. If an Anthropic API key is
 * configured, the AI synthesizes a human-readable rationale over the
 * deterministic facts. Steps, recommended version, assessment, and confidence
 * always come from the deterministic planner so the output remains predictable.
 */
@Component
public class AiMigrationAdvisor {

    private static final String SYSTEM_PROMPT = """
            You are API Sentinel's governance assistant. Given a structured set of
            API contract changes and the deterministic risk assessment, produce a
            short (3-6 sentences) rationale that explains the business and technical
            impact in plain language. Do not invent facts. Do not change the
            classification. Do not output JSON.
            """;

    private final MigrationPlanner deterministicPlanner;
    private final AnthropicChatClient chatClient;

    public AiMigrationAdvisor(MigrationPlanner deterministicPlanner, AnthropicChatClient chatClient) {
        this.deterministicPlanner = deterministicPlanner;
        this.chatClient = chatClient;
    }

    public MigrationPlan advise(MigrationContext context) {
        MigrationPlan baseline = deterministicPlanner.plan(context.contractId(), context.apiName(),
                context.oldVersion(), context.changes(), context.consumers());
        if (!chatClient.isAvailable()) {
            return baseline;
        }
        String userPrompt = buildPrompt(context, baseline);
        String aiRationale = chatClient.complete(SYSTEM_PROMPT, userPrompt);
        if (aiRationale == null || aiRationale.isBlank()) {
            return baseline;
        }
        return new MigrationPlan(baseline.id(), baseline.contractId(), baseline.assessment(),
                aiRationale.trim(), baseline.steps(), baseline.recommendedVersion(), baseline.confidence());
    }

    private String buildPrompt(MigrationContext context, MigrationPlan baseline) {
        long breakingCount = context.changes().stream().filter(ContractChange::breaking).count();
        StringBuilder sb = new StringBuilder();
        sb.append("API: ").append(context.apiName()).append('\n');
        sb.append("Current version: ").append(context.oldVersion()).append('\n');
        sb.append("Risk score: ").append(context.riskScore()).append("/100\n");
        sb.append("Consumers: ").append(context.consumers().size())
                .append(" (").append(String.join(", ", context.consumers())).append(")\n");
        sb.append("Breaking changes: ").append(breakingCount).append('\n');
        sb.append("Deterministic assessment: ").append(baseline.assessment()).append('\n');
        sb.append("Deterministic rationale: ").append(baseline.rationale()).append('\n');
        sb.append("Changes:\n");
        for (ContractChange c : context.changes()) {
            sb.append("  - [").append(c.severity()).append("] ")
                    .append(c.breaking() ? "BREAKING " : "")
                    .append(c.changeType()).append(" at ").append(c.path())
                    .append(": ").append(c.description()).append('\n');
        }
        return sb.toString();
    }
}