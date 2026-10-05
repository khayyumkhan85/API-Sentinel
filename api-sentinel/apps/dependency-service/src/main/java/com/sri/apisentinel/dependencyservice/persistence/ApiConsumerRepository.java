package com.sri.apisentinel.dependencyservice.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ApiConsumerRepository extends JpaRepository<ApiConsumerEntity, UUID> {
    List<ApiConsumerEntity> findByApiName(String apiName);
    List<ApiConsumerEntity> findByServiceName(String serviceName);
}