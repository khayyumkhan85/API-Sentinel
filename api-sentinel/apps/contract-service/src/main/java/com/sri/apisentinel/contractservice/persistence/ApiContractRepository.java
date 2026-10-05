package com.sri.apisentinel.contractservice.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApiContractRepository extends JpaRepository<ApiContractEntity, UUID> {

    List<ApiContractEntity> findByName(String name);

    Optional<ApiContractEntity> findByNameAndVersion(String name, String version);
}