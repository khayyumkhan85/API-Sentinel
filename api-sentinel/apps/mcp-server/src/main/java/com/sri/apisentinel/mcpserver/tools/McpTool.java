package com.sri.apisentinel.mcpserver.tools;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an MCP tool exposed to AI agents.
 * In a full Spring AI setup, this would wire into Spring AI's MCP server.
 * For now, {@link McpToolRegistry} discovers these at startup and exposes
 * them over a JSON-RPC-style HTTP endpoint.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface McpTool {
    String name();
    String description();
}