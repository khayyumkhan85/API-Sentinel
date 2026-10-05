package com.sri.apisentinel.mcpserver.tools;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class McpToolRegistry {

    private static final Logger log = LoggerFactory.getLogger(McpToolRegistry.class);

    private final ApplicationContext context;
    private final Map<String, RegisteredTool> tools = new LinkedHashMap<>();

    public McpToolRegistry(ApplicationContext context) {
        this.context = context;
    }

    @PostConstruct
    public void discover() {
        for (String name : context.getBeanDefinitionNames()) {
            Object bean = context.getBean(name);
            Class<?> clazz = bean.getClass();
            for (Method method : clazz.getMethods()) {
                McpTool annotation = method.getAnnotation(McpTool.class);
                if (annotation != null) {
                    RegisteredTool tool = new RegisteredTool(annotation.name(), annotation.description(),
                            bean, method);
                    tools.put(annotation.name(), tool);
                    log.info("Registered MCP tool: {}", annotation.name());
                }
            }
        }
    }

    public List<Map<String, String>> listTools() {
        return tools.values().stream()
                .map(t -> Map.of("name", t.name(), "description", t.description()))
                .toList();
    }

    public RegisteredTool get(String name) {
        return tools.get(name);
    }

    public record RegisteredTool(String name, String description, Object bean, Method method) {}
}