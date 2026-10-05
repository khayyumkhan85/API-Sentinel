# MCP Integration

API Sentinel exposes its capabilities as MCP tools so AI coding agents can ask governance questions directly from their editor loop.

## Transport

The `mcp-server` application exposes a minimal JSON-RPC-style endpoint:

- `GET  /mcp/tools` — list registered tools
- `POST /mcp/invoke/{tool}` — invoke, passing a JSON array of arguments

In a Spring AI MCP deployment this would be fronted by the Streamable HTTP MCP server; the shape here matches.

## Registered tools

Declared via the `@McpTool` annotation on `SentinelTools`:

| Tool | Description |
|---|---|
| `analyzeApiChange` | Diff two OpenAPI specs, return the classified change list. |
| `getRiskScore` | 0–100 risk score for a change set + consumer count. |
| `findAffectedConsumers` | Services that depend on the given API name. |
| `registerConsumer` | Record that a service consumes an API version. |
| `assessImpact` | Full impact assessment including recommendation. |
| `createMigrationPlan` | Step-by-step migration plan. |
| `classifySeverity` | Highest severity across a set of changes. |

Discovery is dynamic — `McpToolRegistry` scans beans at startup for `@McpTool` methods and registers them. Adding a new tool means adding one annotated method.

## Example agent flow

```
Agent: "Can I remove `customerName` from Customer API?"

1. agent calls analyzeApiChange(oldSpec, newSpec)
   → [{breaking: true, severity: CRITICAL, changeType: REQUIRED_PROPERTY_REMOVED, ...}]

2. agent calls findAffectedConsumers("Customer API")
   → ["order-service", "billing-service", "mobile-app"]

3. agent calls getRiskScore(changes, 3)
   → 94

4. agent calls createMigrationPlan(..., changes, consumers)
   → { assessment: BREAKING_CHANGE,
       recommendedVersion: 2.0.0,
       steps: [ "Keep Customer API 1.0.0 available", ... ] }

Agent → human: "No. Risk 94. Introduce Customer API v2 instead."
```

## Security

The configuration surface for authentication is in place:

```yaml
apisentinel:
  mcp:
    auth:
      mode: ${MCP_AUTH_MODE:api-key}
      apiKey: ${MCP_API_KEY:changeme}
```

Any production deployment should swap this for OAuth2 (bearer token) and front the MCP endpoint with the same authorization server used for the gateway. MCP exposes capability — treat the tool list like any other authenticated API surface.