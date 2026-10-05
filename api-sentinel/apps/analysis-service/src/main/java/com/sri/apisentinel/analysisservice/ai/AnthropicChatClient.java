package com.sri.apisentinel.analysisservice.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

/**
 * Direct HTTP client for Anthropic's Messages API. Keeps the dependency surface small
 * and avoids pulling in Spring AI milestones. Returns null when disabled or when the
 * call fails, which lets {@link AiMigrationAdvisor} fall back to the deterministic plan.
 */
@Component
public class AnthropicChatClient {

    private static final Logger log = LoggerFactory.getLogger(AnthropicChatClient.class);
    private static final String ENDPOINT = "https://api.anthropic.com/v1/messages";

    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @Value("${apisentinel.ai.enabled:false}")
    private boolean enabled;

    @Value("${apisentinel.ai.model:claude-sonnet-5-5}")
    private String model;

    @Value("${apisentinel.ai.maxTokens:1024}")
    private int maxTokens;

    @Value("${ANTHROPIC_API_KEY:}")
    private String apiKey;

    public boolean isAvailable() {
        return enabled && apiKey != null && !apiKey.isBlank();
    }

    public String complete(String systemPrompt, String userPrompt) {
        if (!isAvailable()) {
            return null;
        }
        try {
            Map<String, Object> body = Map.of(
                    "model", model,
                    "max_tokens", maxTokens,
                    "system", systemPrompt,
                    "messages", List.of(Map.of("role", "user", "content", userPrompt))
            );
            HttpRequest req = HttpRequest.newBuilder(URI.create(ENDPOINT))
                    .timeout(Duration.ofSeconds(30))
                    .header("content-type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                log.warn("Anthropic call failed status={} body={}", resp.statusCode(), resp.body());
                return null;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> parsed = mapper.readValue(resp.body(), Map.class);
            Object content = parsed.get("content");
            if (content instanceof List<?> list && !list.isEmpty() && list.get(0) instanceof Map<?, ?> first) {
                Object text = first.get("text");
                return text == null ? null : text.toString();
            }
            return null;
        } catch (Exception e) {
            log.warn("Anthropic call errored: {}", e.getMessage());
            return null;
        }
    }
}