# Architecture

API Sentinel is a Spring Boot microservice platform that treats **API contracts as first-class, versioned artifacts** and enforces governance decisions on top of them.

## Services

| Service | Port | Responsibility |
|---|---|---|
| api-gateway | 8080 | Single entry point. Routes to downstream services. |
| contract-service | 8081 | Register OpenAPI specs, store versions, run diffs. |
| dependency-service | 8082 | Track who-consumes-what. Serves the dependency graph. |
| analysis-service | 8083 | Risk scoring, impact assessment, AI migration planner. |
| mcp-server | 8084 | Exposes Sentinel capabilities as MCP tools to AI agents. |

## Data stores

- **PostgreSQL** — durable state (contracts, consumers, changes, assessments). Schema managed by Flyway (`V1__initial_schema.sql` in each service).
- **Redis** — caching layer in front of expensive reads (diffs, consumer lookups).
- **Kafka** — event bus. Topics:
  - `contract.registered`
  - `contract.changed`
  - `contract.breaking-change.detected`

## Request paths

### Register contract
```
POST /api/v1/contracts (gateway → contract-service)
 → OpenApiParser validates
 → persists in PostgreSQL
 → emits contract.registered on Kafka
```

### Compare two versions
```
POST /api/v1/contracts/{old}/compare/{new}
 → loads both specs
 → OpenApiDiffer walks paths + schemas
 → classifies each SchemaChange as breaking / non-breaking + severity
 → persists ContractChange rows
 → emits contract.breaking-change.detected if any
```

### Impact assessment
```
POST /api/v1/analysis/impact
 → gets consumers from dependency-service
 → RiskScoreCalculator produces 0-100 score
 → ImpactAnalyzer produces recommendation
```

## Deterministic vs AI boundary

Everything that would be dangerous to hallucinate is **deterministic Java**:
- OpenAPI parsing (`OpenApiParser`)
- Semantic diff (`OpenApiDiffer`)
- Breaking-change classification (`SchemaChange.breaking` / `.nonBreaking`)
- Risk score (`RiskScoreCalculator`)
- Dependency fan-out (`DependencyGraph`)

AI (`AiMigrationAdvisor` in `analysis-service`) only operates on the structured output of the deterministic pipeline — it synthesizes rationale and migration narrative, never classification.

## Deployment

- **Local**: `docker compose up` (postgres + redis + kafka + all services + prometheus)
- **Kubernetes**: `kubectl apply -f infrastructure/kubernetes/`
- **Observability**: Micrometer → `/actuator/prometheus` on every service → Prometheus scrape config in `infrastructure/prometheus/prometheus.yml`.