package com.sri.apisentinel.riskengine;

import com.sri.apisentinel.commonmodel.ChangeSeverity;
import com.sri.apisentinel.commonmodel.ContractChange;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RiskScoreCalculatorTest {

    private final RiskScoreCalculator calculator = new RiskScoreCalculator();

    @Test
    void returnsZeroWhenNoChanges() {
        assertThat(calculator.calculate(List.of(), 0)).isZero();
    }

    @Test
    void criticalBreakingChangesProduceHighScore() {
        UUID cid = UUID.randomUUID();
        List<ContractChange> changes = List.of(
                ContractChange.of(cid, "/customers/{id}", "GET", "REQUIRED_PROPERTY_REMOVED",
                        ChangeSeverity.CRITICAL, true, "customerName removed")
        );
        int score = calculator.calculate(changes, 3);
        assertThat(score).isGreaterThanOrEqualTo(50);
        assertThat(calculator.band(score))
                .isIn(RiskScoreCalculator.RiskBand.HIGH, RiskScoreCalculator.RiskBand.CRITICAL);
    }

    @Test
    void nonBreakingAdditiveChangeStaysLow() {
        UUID cid = UUID.randomUUID();
        List<ContractChange> changes = List.of(
                ContractChange.of(cid, "/customers/{id}", "GET", "PROPERTY_ADDED",
                        ChangeSeverity.NONE, false, "email added")
        );
        assertThat(calculator.calculate(changes, 0)).isZero();
    }
}