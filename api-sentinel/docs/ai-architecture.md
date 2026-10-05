# AI Architecture

API Sentinel keeps a hard boundary between **deterministic engineering** and **AI reasoning**.

## Deterministic layer (always on)

| Component | Lives in | Role |
|---|---|---|
| `OpenApiParser` | `libs/openapi-diff` | Validate the spec is well-formed OpenAPI 3. |
| `OpenApiDiffer` | `libs/openapi-diff` | Walk paths + schemas, emit `SchemaChange` records. |
| `SchemaChange.breaking / nonBreaking` | `libs/openapi-diff` | Rule-based severity + breaking classification. |
| `RiskScoreCalculator` | `libs/risk-engine` | Numeric 0–100 risk score. |
| `DependencyGraph` | `libs/risk-engine` | API → consumer fan-out. |
| `ImpactAnalyzer` | `libs/risk-engine` | Business-impact summary + recommendation. |
| `MigrationPlanner` | `libs/risk-engine` | Deterministic migration steps + version bump. |
| `ContractTestGenerator` | `libs/risk-engine` | Emits REST-assured style contract tests. |

If AI is turned off, API Sentinel still works end-to-end. The AI layer only *augments*.

## AI layer

`apps/analysis-service/src/main/java/.../ai/AiMigrationAdvisor.java` is the only place where AI is called.

### Input (always structured)

```java
record MigrationContext(
    UUID contractId,
    String apiName,
    String oldVersion,
    String newVersion,
    List<ContractChange> changes,  // produced deterministically
    List<String> consumers,        // produced deterministically
    int riskScore                  // produced deterministically
) {}
```

### Output

A `MigrationPlan` where the AI replaces the `rationale` field with a human-readable narrative. Steps, recommended version, assessment, and confidence all come from the deterministic planner — the AI never fabricates these.

### Toggle

```yaml
apisentinel:
  ai:
    enabled: ${AI_ENABLED:false}
    provider: ${AI_PROVIDER:stub}
    model: ${AI_MODEL:claude-sonnet-5-5}
```

With `enabled=false` the service returns the deterministic plan verbatim. This makes behavior test-friendly and makes failure modes legible: an outage of the LLM is a loss of narrative, not a loss of correctness.

## Why this split

An LLM asked "is this breaking?" over raw YAML will sometimes say yes and sometimes say no on the *same input*. A rule engine will not. Governance decisions must be reproducible — reviewers need to be able to predict the system's answer before they ask it.