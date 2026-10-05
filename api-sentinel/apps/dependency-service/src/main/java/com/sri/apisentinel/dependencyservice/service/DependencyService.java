package com.sri.apisentinel.dependencyservice.service;

import com.sri.apisentinel.commonmodel.ApiConsumer;
import com.sri.apisentinel.dependencyservice.persistence.ApiConsumerEntity;
import com.sri.apisentinel.dependencyservice.persistence.ApiConsumerRepository;
import com.sri.apisentinel.riskengine.DependencyGraph;
import jakarta.annotation.PostConstruct;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DependencyService {

    private final ApiConsumerRepository repository;
    private final DependencyGraph graph;

    public DependencyService(ApiConsumerRepository repository, DependencyGraph graph) {
        this.repository = repository;
        this.graph = graph;
    }

    @PostConstruct
    public void preload() {
        for (ApiConsumerEntity e : repository.findAll()) {
            graph.register(toModel(e));
        }
    }

    @Transactional
    public ApiConsumer register(String serviceName, String apiName, String apiVersion,
                                String contactOwner, String environment) {
        ApiConsumerEntity entity = new ApiConsumerEntity();
        entity.setId(UUID.randomUUID());
        entity.setServiceName(serviceName);
        entity.setApiName(apiName);
        entity.setApiVersion(apiVersion);
        entity.setContactOwner(contactOwner);
        entity.setEnvironment(environment == null ? "prod" : environment);
        entity.setCreatedAt(Instant.now());
        ApiConsumerEntity saved = repository.save(entity);
        ApiConsumer model = toModel(saved);
        graph.register(model);
        return model;
    }

    @Cacheable(value = "consumers", key = "#apiName")
    @Transactional(readOnly = true)
    public List<ApiConsumer> consumersFor(String apiName) {
        return repository.findByApiName(apiName).stream().map(this::toModel).toList();
    }

    @Transactional(readOnly = true)
    public List<ApiConsumer> apisFor(String serviceName) {
        return repository.findByServiceName(serviceName).stream().map(this::toModel).toList();
    }

    public int fanOut(String apiName) {
        return graph.fanOut(apiName);
    }

    private ApiConsumer toModel(ApiConsumerEntity e) {
        return new ApiConsumer(e.getId(), e.getServiceName(), e.getApiName(),
                e.getApiVersion(), e.getContactOwner(), e.getEnvironment());
    }
}