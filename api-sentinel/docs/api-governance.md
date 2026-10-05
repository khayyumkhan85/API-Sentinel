# API Governance

API Sentinel's job is to turn API change into a **decision**, not a silent diff.

## Change severity ladder

| Severity | Examples | Breaking? |
|---|---|---|
| CRITICAL | required property removed, endpoint removed, operation removed | yes |
| HIGH | property type changed, enum value removed, new required request parameter | yes |
| MEDIUM | required property added, parameter removed | yes |
| LOW | deprecation marker added | no |
| NONE | optional property added, endpoint added, enum value added, response code added | no |

Implemented in `libs/openapi-diff/src/main/java/.../SchemaChange.java`.

## Risk score formula

`RiskScoreCalculator` (in `libs/risk-engine`):

```
score = Σ severityWeight(change)
      + 10 × (count of breaking changes)
      + min(consumers × 3, 30)
```

Banded:
- 80–100 → CRITICAL (block)
- 50–79 → HIGH (require migration plan)
- 25–49 → MEDIUM (review)
- 1–24 → LOW (approve with notes)
- 0 → NONE (approve)

## Governance decision

`ImpactAnalyzer.recommend(score, breaking)`:

```
no breaking changes             → APPROVE
breaking + score ≥ 80           → BLOCK, introduce new major version
breaking + score ≥ 50           → REVIEW, coordinate migration
breaking + score < 50           → APPROVE WITH CAUTION
```

## CI integration pattern

Although there is no committed CI pipeline file (keeps the repo scoped to the provided structure), any CI runner can call:

```
POST /api/v1/contracts/{oldId}/compare/{newId}
POST /api/v1/analysis/impact
```

and fail the build when `recommendation == "BLOCK"`.

## Versioning guidance

The `MigrationPlanner` emits a recommended next version:
- breaking → bump **major** (e.g. 1.4.2 → 2.0.0)
- non-breaking → bump **minor** (e.g. 1.4.2 → 1.5.0)

## Deprecation window

Convention: an API version flagged `DEPRECATED` must stay available for **90 days** before being retired (`MigrationPlanner.buildSteps`).