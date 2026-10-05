package com.sri.apisentinel.riskengine;

import com.sri.apisentinel.commonmodel.ApiConsumer;

import java.util.*;

public class DependencyGraph {

    private final Map<String, Set<String>> apiToConsumers = new HashMap<>();
    private final Map<String, Set<String>> consumerToApis = new HashMap<>();

    public synchronized void register(ApiConsumer consumer) {
        apiToConsumers.computeIfAbsent(consumer.apiName(), k -> new HashSet<>()).add(consumer.serviceName());
        consumerToApis.computeIfAbsent(consumer.serviceName(), k -> new HashSet<>()).add(consumer.apiName());
    }

    public synchronized Set<String> consumersOf(String apiName) {
        return new HashSet<>(apiToConsumers.getOrDefault(apiName, Set.of()));
    }

    public synchronized Set<String> apisUsedBy(String serviceName) {
        return new HashSet<>(consumerToApis.getOrDefault(serviceName, Set.of()));
    }

    public synchronized int fanOut(String apiName) {
        return apiToConsumers.getOrDefault(apiName, Set.of()).size();
    }

    public synchronized void clear() {
        apiToConsumers.clear();
        consumerToApis.clear();
    }
}