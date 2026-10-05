package com.sri.apisentinel.commonmodel;

import java.util.UUID;

public record ApiConsumer(
        UUID id,
        String serviceName,
        String apiName,
        String apiVersion,
        String contactOwner,
        String environment
) {
    public static ApiConsumer of(String serviceName, String apiName, String apiVersion) {
        return new ApiConsumer(UUID.randomUUID(), serviceName, apiName, apiVersion, null, "prod");
    }
}