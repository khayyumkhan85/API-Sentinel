package com.sri.apisentinel.analysisservice.service;

import com.sri.apisentinel.analysisservice.ai.AiMigrationAdvisor;
import com.sri.apisentinel.analysisservice.ai.AnthropicChatClient;
import com.sri.apisentinel.commonmodel.ApiConsumer;
import com.sri.apisentinel.commonmodel.ChangeSeverity;
import com.sri.apisentinel.commonmodel.ContractChange;
import com.sri.apisentinel.commonmodel.ImpactAssessment;
import com.sri.apisentinel.commonmodel.MigrationPlan;
import com.sri.apisentinel.riskengine.ImpactAnalyzer;
import com.sri.apisentinel.riskengine.MigrationPlanner;
import com.sri.apisentinel.riskengine.RiskScoreCalculator;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AnalysisServiceTest {

    private final AnthropicChatClient disabledClient = disabledClient();
    private final AnalysisService service = new AnalysisService(
            new RiskScoreCalculator(),
            new ImpactAnalyzer(),
            new AiMigrationAdvisor(new MigrationPlanner(), disabledClient));

    private static AnthropicChatClient disabledClient() {
        AnthropicChatClient c = mock(AnthropicChatClient.class);
        when(c.isAvailable()).thenReturn(false);
        return c;
    }

    @Test
    void assessesBreakingChangeImpact() {
        UUID cid = UUID.randomUUID();
        List<ContractChange> changes = List.of(
                ContractChange.of(cid, "/customers/{id}", "GET", "REQUIRED_PROPERTY_REMOVED",
                        ChangeSeverity.CRITICAL, true, "customerName removed"));
        List<ApiConsumer> consumers = List.of(
                ApiConsumer.of("order-service", "Customer API", "1.0.0"),
                ApiConsumer.of("mobile-app", "Customer API", "1.0.0"));

        ImpactAssessment impact = service.assess(changes, consumers);

        assertThat(impact.riskScore()).isGreaterThan(0);
        assertThat(impact.recommendation()).isNotBlank();
    }

    @Test
    void producesMigrationPlanForBreakingChange() {
        UUID cid = UUID.randomUUID();
        List<ContractChange> changes = List.of(
                ContractChange.of(cid, "/customers/{id}", "GET", "REQUIRED_PROPERTY_REMOVED",
                        ChangeSeverity.CRITICAL, true, "customerName removed"));

        MigrationPlan plan = service.migrationPlan(cid, "Customer API", "1.0.0",
                changes, List.of("order-service", "mobile-app"));

        assertThat(plan.assessment()).isEqualTo("BREAKING_CHANGE");
        assertThat(plan.steps()).isNotEmpty();
    }
}