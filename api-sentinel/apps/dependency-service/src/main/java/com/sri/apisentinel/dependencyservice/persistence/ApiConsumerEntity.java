package com.sri.apisentinel.dependencyservice.persistence;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "api_consumer",
        uniqueConstraints = @UniqueConstraint(columnNames = {"service_name", "api_name", "api_version"}))
public class ApiConsumerEntity {

    @Id
    private UUID id;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "api_name", nullable = false)
    private String apiName;

    @Column(name = "api_version", nullable = false)
    private String apiVersion;

    @Column(name = "contact_owner")
    private String contactOwner;

    @Column(nullable = false)
    private String environment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public ApiConsumerEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getServiceName() { return serviceName; }
    public void setServiceName(String s) { this.serviceName = s; }
    public String getApiName() { return apiName; }
    public void setApiName(String a) { this.apiName = a; }
    public String getApiVersion() { return apiVersion; }
    public void setApiVersion(String v) { this.apiVersion = v; }
    public String getContactOwner() { return contactOwner; }
    public void setContactOwner(String c) { this.contactOwner = c; }
    public String getEnvironment() { return environment; }
    public void setEnvironment(String e) { this.environment = e; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant c) { this.createdAt = c; }
}