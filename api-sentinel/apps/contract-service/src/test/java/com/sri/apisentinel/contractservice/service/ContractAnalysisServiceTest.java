package com.sri.apisentinel.contractservice.service;

import com.sri.apisentinel.commonmodel.ApiContract;
import com.sri.apisentinel.commonmodel.ContractChange;
import com.sri.apisentinel.commonmodel.ContractStatus;
import com.sri.apisentinel.contractservice.event.ContractEventPublisher;
import com.sri.apisentinel.openapidiff.ContractDiffService;
import com.sri.apisentinel.testsupport.Fixtures;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ContractAnalysisServiceTest {

    @Test
    void publishesBreakingChangeEventWhenDetected() {
        ContractService contractService = mock(ContractService.class);
        ContractEventPublisher publisher = mock(ContractEventPublisher.class);
        ContractDiffService diffService = new ContractDiffService();

        UUID oldId = UUID.randomUUID();
        UUID newId = UUID.randomUUID();
        Instant now = Instant.now();

        when(contractService.get(oldId)).thenReturn(new ApiContract(oldId, "Customer API", "1.0.0",
                null, null, ContractStatus.ACTIVE, Fixtures.CUSTOMER_V1, now, now));
        when(contractService.get(newId)).thenReturn(new ApiContract(newId, "Customer API", "2.0.0",
                null, null, ContractStatus.ACTIVE, Fixtures.CUSTOMER_V2_BREAKING, now, now));

        ContractAnalysisService service = new ContractAnalysisService(contractService, diffService, publisher);
        List<ContractChange> changes = service.compare(oldId, newId);

        assertThat(changes).anyMatch(ContractChange::breaking);
        verify(publisher).publishBreakingChange(any(), any());
    }
}