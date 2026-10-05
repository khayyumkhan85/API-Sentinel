package com.sri.apisentinel.contractservice.service;

import com.sri.apisentinel.commonmodel.ApiContract;
import com.sri.apisentinel.commonmodel.ContractStatus;
import com.sri.apisentinel.contractservice.event.ContractEventPublisher;
import com.sri.apisentinel.contractservice.persistence.ApiContractEntity;
import com.sri.apisentinel.contractservice.persistence.ApiContractRepository;
import com.sri.apisentinel.openapidiff.OpenApiParser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class ContractService {

    private final ApiContractRepository repository;
    private final OpenApiParser parser;
    private final ContractEventPublisher publisher;

    public ContractService(ApiContractRepository repository,
                           OpenApiParser parser,
                           ContractEventPublisher publisher) {
        this.repository = repository;
        this.parser = parser;
        this.publisher = publisher;
    }

    @Transactional
    public ApiContract register(String name, String version, String specification, String owner, String repoUrl) {
        parser.parse(specification);
        ApiContractEntity entity = new ApiContractEntity();
        entity.setId(UUID.randomUUID());
        entity.setName(name);
        entity.setVersion(version);
        entity.setOwner(owner);
        entity.setRepository(repoUrl);
        entity.setStatus(ContractStatus.ACTIVE);
        entity.setSpecification(specification);
        Instant now = Instant.now();
        entity.setCreatedAt(now);
        entity.setUpdatedAt(now);
        ApiContractEntity saved = repository.save(entity);
        ApiContract contract = toModel(saved);
        publisher.publishRegistered(contract);
        return contract;
    }

    @Transactional(readOnly = true)
    public ApiContract get(UUID id) {
        return repository.findById(id).map(this::toModel)
                .orElseThrow(() -> new NoSuchElementException("Contract not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<ApiContract> listByName(String name) {
        return repository.findByName(name).stream().map(this::toModel).toList();
    }

    @Transactional(readOnly = true)
    public List<ApiContract> listAll() {
        return repository.findAll().stream().map(this::toModel).toList();
    }

    private ApiContract toModel(ApiContractEntity e) {
        return new ApiContract(e.getId(), e.getName(), e.getVersion(), e.getOwner(),
                e.getRepository(), e.getStatus(), e.getSpecification(),
                e.getCreatedAt(), e.getUpdatedAt());
    }
}