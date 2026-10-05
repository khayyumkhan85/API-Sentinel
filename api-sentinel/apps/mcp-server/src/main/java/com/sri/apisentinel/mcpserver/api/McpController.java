package com.sri.apisentinel.mcpserver.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sri.apisentinel.mcpserver.tools.McpToolRegistry;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Lightweight JSON-RPC-style endpoint for MCP tool invocation.
 * A production deployment would front this with Spring AI's MCP server
 * (Streamable HTTP) and OAuth2; this endpoint matches that shape.
 */
@RestController
@RequestMapping(value = "/mcp", produces = MediaType.APPLICATION_JSON_VALUE)
public class McpController {

    private final McpToolRegistry registry;
    private final ObjectMapper objectMapper;

    public McpController(McpToolRegistry registry, ObjectMapper objectMapper) {
        this.registry = registry;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/tools")
    public List<Map<String, String>> list() {
        return registry.listTools();
    }

    @PostMapping(value = "/invoke/{tool}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Object> invoke(@PathVariable String tool,
                                         @RequestBody(required = false) List<Object> args) {
        McpToolRegistry.RegisteredTool registered = registry.get(tool);
        if (registered == null) {
            return ResponseEntity.status(404).body(Map.of("error", "unknown tool: " + tool));
        }
        try {
            Method method = registered.method();
            Class<?>[] paramTypes = method.getParameterTypes();
            Object[] resolvedArgs = coerceArgs(args, paramTypes);
            Object result = method.invoke(registered.bean(), resolvedArgs);
            return ResponseEntity.ok(result);
        } catch (InvocationTargetException ite) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "tool failed",
                    "message", ite.getCause() == null ? ite.getMessage() : ite.getCause().getMessage()
            ));
        } catch (ReflectiveOperationException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    private Object[] coerceArgs(List<Object> args, Class<?>[] paramTypes) {
        if (args == null) args = List.of();
        Object[] out = new Object[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            Object raw = i < args.size() ? args.get(i) : null;
            out[i] = objectMapper.convertValue(raw, paramTypes[i]);
        }
        return out;
    }
}