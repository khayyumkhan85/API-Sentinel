# Demo Scenarios

Three end-to-end scenarios that exercise the whole platform.

## Scenario 1 — Breaking change detected

**Setup:** register `contracts/customer/customer-v1.yaml` then `contracts/customer/customer-v2.yaml`.

```bash
# start the stack
docker compose up --build -d

# register v1
V1=$(curl -s -X POST http://localhost:8081/api/v1/contracts \
  -H 'Content-Type: application/json' \
  -d "$(jq -n --arg s "$(cat contracts/customer/customer-v1.yaml)" \
         '{name:"Customer API", version:"1.0.0", specification:$s}')" | jq -r .id)

# register v2
V2=$(curl -s -X POST http://localhost:8081/api/v1/contracts \
  -H 'Content-Type: application/json' \
  -d "$(jq -n --arg s "$(cat contracts/customer/customer-v2.yaml)" \
         '{name:"Customer API", version:"2.0.0", specification:$s}')" | jq -r .id)

# register three consumers
for svc in order-service billing-service mobile-app; do
  curl -s -X POST http://localhost:8082/api/v1/consumers \
    -H 'Content-Type: application/json' \
    -d "{\"serviceName\":\"$svc\",\"apiName\":\"Customer API\",\"apiVersion\":\"1.0.0\"}"
done

# compare — this emits contract.breaking-change.detected on Kafka
curl -s -X POST http://localhost:8081/api/v1/contracts/$V1/compare/$V2 | jq
```

**Expected:** the diff lists `REQUIRED_PROPERTY_REMOVED` for `customerName` with severity `CRITICAL`, and the Kafka topic `contract.breaking-change.detected` receives an event.

## Scenario 2 — Non-breaking additive change

```bash
# diff v1 against test-fixtures/non-breaking/customer-added-optional-field.yaml
# (register the second spec, then call compare)
```

**Expected:** the diff lists `PROPERTY_ADDED` with severity `NONE`, `breaking=false`. Risk score 0. Topic emitted is `contract.changed`, not the breaking topic.

## Scenario 3 — AI migration plan

```bash
curl -s -X POST http://localhost:8083/api/v1/analysis/migration-plan \
  -H 'Content-Type: application/json' \
  -d '{
    "contractId":"'$V2'",
    "apiName":"Customer API",
    "currentVersion":"1.0.0",
    "changes":[{
      "id":"00000000-0000-0000-0000-000000000001",
      "contractId":"'$V2'",
      "severity":"CRITICAL",
      "breaking":true,
      "path":"#/components/schemas/Customer",
      "operation":null,
      "changeType":"REQUIRED_PROPERTY_REMOVED",
      "description":"customerName removed"
    }],
    "consumers":["order-service","billing-service","mobile-app"]
  }' | jq
```

**Expected:**
```json
{
  "assessment": "BREAKING_CHANGE",
  "recommendedVersion": "2.0.0",
  "confidence": 0.92,
  "steps": [
    "Keep Customer API 1.0.0 available",
    "Publish Customer API 2.0.0 with new contract",
    "Migrate consumer: order-service",
    "Migrate consumer: billing-service",
    "Migrate consumer: mobile-app",
    "Monitor adoption of 2.0.0",
    "Mark 1.0.0 as deprecated after 90 days"
  ]
}
```

With `AI_ENABLED=true`, the `rationale` field is rewritten by the AI advisor; steps and version remain deterministic.

## Scenario 4 — AI agent via MCP

```bash
curl -s http://localhost:8084/mcp/tools | jq

curl -s -X POST http://localhost:8084/mcp/invoke/getRiskScore \
  -H 'Content-Type: application/json' \
  -d '[[{"id":"...","contractId":"...","severity":"CRITICAL","breaking":true,
         "path":"#/components/schemas/Customer","operation":null,
         "changeType":"REQUIRED_PROPERTY_REMOVED","description":"customerName removed"}], 3]'
```

**Expected:** an integer risk score in the 70–100 range.