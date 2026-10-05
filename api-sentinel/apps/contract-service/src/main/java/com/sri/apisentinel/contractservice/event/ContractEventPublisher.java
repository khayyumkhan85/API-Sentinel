package com.sri.apisentinel.contractservice.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sri.apisentinel.commonmodel.ApiContract;
import com.sri.apisentinel.commonmodel.ContractChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class ContractEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ContractEventPublisher.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${apisentinel.topics.registered:contract.registered}")
    private String registeredTopic;

    @Value("${apisentinel.topics.changed:contract.changed}")
    private String changedTopic;

    @Value("${apisentinel.topics.breaking:contract.breaking-change.detected}")
    private String breakingTopic;

    public ContractEventPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publishRegistered(ApiContract contract) {
        send(registeredTopic, contract.id().toString(), Map.of(
                "eventType", "contract.registered",
                "contractId", contract.id().toString(),
                "api", contract.name(),
                "version", contract.version()
        ));
    }

    public void publishChanged(ApiContract contract, List<ContractChange> changes) {
        send(changedTopic, contract.id().toString(), Map.of(
                "eventType", "contract.changed",
                "contractId", contract.id().toString(),
                "api", contract.name(),
                "version", contract.version(),
                "changeCount", changes.size()
        ));
    }

    public void publishBreakingChange(ApiContract contract, List<ContractChange> changes) {
        send(breakingTopic, contract.id().toString(), Map.of(
                "eventType", "contract.breaking-change.detected",
                "contractId", contract.id().toString(),
                "api", contract.name(),
                "version", contract.version(),
                "breakingCount", changes.stream().filter(ContractChange::breaking).count()
        ));
    }

    private void send(String topic, String key, Map<String, Object> payload) {
        try {
            kafkaTemplate.send(topic, key, objectMapper.writeValueAsString(payload));
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize event to {}: {}", topic, e.getMessage());
        } catch (RuntimeException e) {
            log.warn("Failed to publish event to {}: {}", topic, e.getMessage());
        }
    }
}