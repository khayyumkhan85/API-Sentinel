package com.sri.apisentinel.openapidiff;

import com.sri.apisentinel.commonmodel.ChangeSeverity;
import com.sri.apisentinel.commonmodel.ContractChange;
import com.sri.apisentinel.testsupport.Fixtures;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContractDiffServiceTest {

    private final ContractDiffService service = new ContractDiffService();

    @Test
    void detectsBreakingChangeWhenRequiredPropertyRemoved() {
        List<ContractChange> changes = service.diff(UUID.randomUUID(),
                Fixtures.CUSTOMER_V1, Fixtures.CUSTOMER_V2_BREAKING);

        assertThat(changes).anyMatch(ContractChange::breaking);
        assertThat(service.highestSeverity(changes)).isEqualTo(ChangeSeverity.CRITICAL);
    }

    @Test
    void detectsNoBreakingChangesWhenOptionalFieldAdded() {
        List<ContractChange> changes = service.diff(UUID.randomUUID(),
                Fixtures.CUSTOMER_V1, Fixtures.CUSTOMER_V1_NON_BREAKING);

        assertThat(changes).noneMatch(ContractChange::breaking);
    }

    @Test
    void returnsEmptyWhenNoDifferences() {
        List<ContractChange> changes = service.diff(UUID.randomUUID(),
                Fixtures.CUSTOMER_V1, Fixtures.CUSTOMER_V1);
        assertThat(changes).isEmpty();
    }
}