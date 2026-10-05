# API Sentinel

**AI-Powered API Contract Intelligence & Governance Platform**

API Sentinel detects API contract drift, identifies breaking changes, analyzes downstream consumer impact, generates contract tests, and uses AI to explain whether an API change is safe to ship.

## Why

Teams change OpenAPI specs faster than ever — AI coding tools accelerate the problem. A deterministic diff tells you *what* changed. API Sentinel tells you:

- Is this change **breaking**?
- **Who** depends on it?
- **How risky** is the rollout? (0–100)
- **What's the recommended migration path?**
- Can we **generate contract tests** to prove compatibility?

## Architecture

```
          ┌─────────────┐
          │  React UI   │
          └──────┬──────┘
                 ▼
          ┌─────────────┐
          │ API Gateway │
          └──────┬──────┘
      ┌─────────┼──────────┬──────────┐
      ▼         ▼          ▼          ▼
Contract   Dependency   Analysis    MCP
Service    Service      Service     Server
   │           │           │
   ▼           ▼           ▼
PostgreSQL   Redis       Kafka
```

Full detail: `docs/architecture.md`.

## Module Layout

- **apps/** — deployable Spring Boot services
  - `api-gateway` — single entry point (Spring Cloud Gateway)
  - `contract-service` — register / version / diff OpenAPI specs
  - `dependency-service` — API ↔ consumer graph
  - `analysis-service` — risk scoring, impact assessment, AI migration planner
  - `mcp-server` — exposes Sentinel capabilities as MCP tools for AI agents
- **libs/** — shared libraries
  - `common-model` — core records (ApiContract, ContractChange, ImpactAssessment, …)
  - `openapi-diff` — OpenAPI parser + semantic diff engine
  - `risk-engine` — deterministic risk scoring, impact analysis, migration planner, test generator, dependency graph
  - `test-support` — fixtures reused across module tests
- **dashboard/** — React + TypeScript UI
- **contracts/** — reference OpenAPI specs (customer, order, payment)
- **test-fixtures/** — breaking / non-breaking / security sample specs
- **infrastructure/** — Docker, Kubernetes, Prometheus
- **docs/** — architecture, governance, AI design, MCP, demo scenarios

## Build

```bash
./gradlew build
```

Each service is independently runnable with `./gradlew :apps:<service>:bootRun`.

## Run Locally (Docker Compose)

```bash
docker compose up --build
```

Services:
- Gateway: http://localhost:8080
- Contract service: http://localhost:8081
- Dependency service: http://localhost:8082
- Analysis service: http://localhost:8083
- MCP server: http://localhost:8084
- Prometheus: http://localhost:9090

## Dashboard

```bash
cd dashboard
npm install
npm run dev
```

## Deploy to Kubernetes

```bash
kubectl apply -f infrastructure/kubernetes/
```

## Example: Detect a breaking change

```bash
# 1. Register v1
curl -X POST http://localhost:8081/api/v1/contracts \
  -H 'Content-Type: application/json' \
  -d @- <<EOF
{"name":"Customer API","version":"1.0.0","specification":"$(cat contracts/customer/customer-v1.yaml | jq -Rs .)"}
EOF

# 2. Register v2
curl -X POST http://localhost:8081/api/v1/contracts \
  -H 'Content-Type: application/json' \
  -d '{...customer-v2.yaml...}'

# 3. Compare — returns the breaking changes
curl -X POST http://localhost:8081/api/v1/contracts/<v1-id>/compare/<v2-id>
```

See `docs/demo-scenarios.md` for the full walkthrough.

## Stack

Java 21 · Gradle Kotlin DSL · Spring Boot 3 · PostgreSQL · Redis · Kafka · React · Testcontainers · Docker · Kubernetes · MCP