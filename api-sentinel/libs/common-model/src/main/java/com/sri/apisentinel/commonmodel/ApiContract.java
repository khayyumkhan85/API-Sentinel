package com.sri.apisentinel.commonmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.util.UUID;

public record ApiContract(
        UUID id,
        String name,
        String version,
        String owner,
        String repository,
        ContractStatus status,
        String specification,
        Instant createdAt,
        Instant updatedAt
) {

    @JsonCreator
    public ApiContract(
            @JsonProperty("id") UUID id,
            @JsonProperty("name") String name,
            @JsonProperty("version") String version,
            @JsonProperty("owner") String owner,
            @JsonProperty("repository") String repository,
            @JsonProperty("status") ContractStatus status,
            @JsonProperty("specification") String specification,
            @JsonProperty("createdAt") Instant createdAt,
            @JsonProperty("updatedAt") Instant updatedAt
    ) {
        this.id = id == null ? UUID.randomUUID() : id;
        this.name = name;
        this.version = version;
        this.owner = owner;
        this.repository = repository;
        this.status = status == null ? ContractStatus.ACTIVE : status;
        this.specification = specification;
        this.createdAt = createdAt == null ? Instant.now() : createdAt;
        this.updatedAt = updatedAt == null ? Instant.now() : updatedAt;
    }

    public static ApiContract of(String name, String version, String specification) {
        Instant now = Instant.now();
        return new ApiContract(UUID.randomUUID(), name, version, null, null,
                ContractStatus.ACTIVE, specification, now, now);
    }
}