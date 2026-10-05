package com.sri.apisentinel.contractservice.service;

import com.sri.apisentinel.commonmodel.ApiContract;
import com.sri.apisentinel.commonmodel.ContractChange;
import com.sri.apisentinel.contractservice.event.ContractEventPublisher;
import com.sri.apisentinel.openapidiff.ContractDiffService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ContractAnalysisService {

    private final ContractService contractService;
    private final ContractDiffService diffService;
    private final ContractEventPublisher publisher;

    public ContractAnalysisService(ContractService contractService,
                                   ContractDiffService diffService,
                                   ContractEventPublisher publisher) {
        this.contractService = contractService;
        this.diffService = diffService;
        this.publisher = publisher;
    }

    @Cacheable(value = "contractDiff", key = "#oldContractId.toString() + ':' + #newContractId.toString()")
    public List<ContractChange> compare(UUID oldContractId, UUID newContractId) {
        ApiContract oldContract = contractService.get(oldContractId);
        ApiContract newContract = contractService.get(newContractId);
        List<ContractChange> changes = diffService.diff(newContractId,
                oldContract.specification(), newContract.specification());
        if (changes.stream().anyMatch(ContractChange::breaking)) {
            publisher.publishBreakingChange(newContract, changes);
        } else {
            publisher.publishChanged(newContract, changes);
        }
        return changes;
    }
}