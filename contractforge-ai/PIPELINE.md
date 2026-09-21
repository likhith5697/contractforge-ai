# `POST /api/pipeline/run` — the one-shot orchestrator

This is a convenience layer over the three phases already documented in [README.md](README.md). It does not
introduce any new capability of its own — it just chains the existing, independently-tested pieces
(`ContractParseService` → `ScenarioGenerationService` → a Test-API execution client) into one HTTP call so you
don't have to parse, then loop `generate` over 20 endpoint ids by hand, then loop the Phase 3 harness over 20
artifact files by hand.

## What it does, in order

1. **Parse** the configured OpenAPI source (same as `POST /api/contracts/parse`) — always fresh, every call.
2. **For every discovered endpoint**, call scenario generation (same as `POST /api/scenarios/generate`) with a
   shared intent and scenario cap, and persist its artifact (same as Phase 2 always does).
3. **For every accepted scenario** in that artifact, send one real HTTP request to the validated target base URL
   and compare actual vs. expected status (the same rules as the Phase 3 harness, reimplemented on Spring's
   `RestClient` instead of RestAssured — see [Why not just call the Phase 3 harness?](#why-not-just-call-the-phase-3-harness) below).
4. **Aggregate** everything into one `PipelineRunReport`, return it in the HTTP response, and also persist it to
   `target/contractforge/reports/pipeline-<runId>.json`.

## Request

Every field is optional — `POST /api/pipeline/run` with an empty (or absent) body runs all defaults.

```json
{
  "intent": "Cover happy path, negative, boundary and business-risk cases",
  "maxScenariosPerEndpoint": 3,
  "targetBaseUrl": "http://localhost:8080"
}
```

| Field | Default | Notes |
|---|---|---|
| `intent` | `"Generate a representative mix of happy path, negative, boundary and business-risk scenarios"` | Passed as-is to every endpoint's generation call. Max 500 characters. |
| `maxScenariosPerEndpoint` | `3` | Clamped to `[1, 8]`, same rule as `POST /api/scenarios/generate`. Kept low by default since this multiplies across every discovered endpoint. |
| `targetBaseUrl` | `http://localhost:8080` | **Must be in the fixed allowlist** (`contractforge.execution.allowed-base-urls`, default also includes `http://127.0.0.1:8080`) or the whole request is rejected with `400` before anything runs — no generation, no execution, no OpenAI spend. |

## Response

```json
{
  "runId": "b17d25d4-d4c6-4bb8-ba14-867722cbe44e",
  "startedAt": "2026-09-21T05:50:40.9Z",
  "completedAt": "2026-09-21T05:50:42.7Z",
  "targetBaseUrl": "http://localhost:8080",
  "totalEndpoints": 20,
  "endpointsWithAcceptedScenarios": 18,
  "totalScenariosExecuted": 47,
  "totalScenariosPassed": 31,
  "totalScenariosFailed": 16,
  "endpoints": [
    {
      "endpointId": "POST:/api/payments/scheduled",
      "generation": {
        "status": "OK",
        "model": "gpt-5-mini",
        "acceptedCount": 1,
        "rejectedCount": 0,
        "artifactId": "321dfad2-5d83-4024-ba67-f7516129d989",
        "artifactPath": "target/contractforge/scenarios/321dfad2-5d83-4024-ba67-f7516129d989.json",
        "error": null
      },
      "execution": {
        "status": "OK",
        "total": 1,
        "passed": 1,
        "failed": 0,
        "results": [
          {
            "scenarioName": "valid_payment",
            "expectedStatus": 201,
            "actualStatus": 201,
            "passed": true,
            "requestId": "270d21b0-6348-47bc-99dd-3b8d7f40c3e9",
            "durationMs": 1299,
            "error": null
          }
        ],
        "error": null
      }
    }
  ]
}
```

This is a real capture from `PipelineOrchestrationServiceTest` (one endpoint, against a local mock server) —
the shape is exactly what a live 20-endpoint run returns, just with 20 entries in `endpoints[]`.

### Per-endpoint outcomes you'll actually see

| `generation.status` | Meaning |
|---|---|
| `OK` | Generation ran; check `acceptedCount`/`rejectedCount` for what came out of it. |
| `FAILED` | This endpoint's OpenAI call failed (timeout, rate limit, malformed output, etc.) — **not fatal to the run**; every other endpoint still gets a chance. `error` has the reason. |

| `execution.status` | Meaning |
|---|---|
| `OK` | At least one accepted scenario was executed; check `results[]`. |
| `SKIPPED_NO_ACCEPTED_SCENARIOS` | Generation produced zero accepted scenarios (e.g. this endpoint has no documented error codes, so no NEGATIVE/BOUNDARY scenario could ever validate — see the honesty note in README.md about `documentedErrorStatusCodes`). Nothing to execute; not an error. |
| `SKIPPED` | Generation itself failed for this endpoint, so there was nothing to execute. |

A missing `OPENAI_API_KEY` is the one failure that **does** abort the whole request immediately with `503` —
every one of the 20 endpoints would fail identically, so there's no value in generating that failure 20 times.

### Why some scenarios genuinely fail execution

Scenario generation only validates against the *schema* — it has no way to know your banking-api database
actually contains the `accountId`/`customerId` values it invents. Running this against a fresh banking-api with
no seeded data, expect to see real `execution.results[].passed: false` entries with `actualStatus: 400`
(`ACCOUNT_NOT_FOUND` etc.) for endpoints like transfers, cards, and KYC that reference an account or customer.
That's correct, expected behavior, not a bug in the pipeline — it's exactly the kind of gap this tool exists to
surface. Seed real customers/accounts first (via banking-api's own APIs) if you want those specific endpoints to
pass end to end.

## Safety model (same guarantees as Phase 3, enforced here too)

- **Allowlist only.** `targetBaseUrl` must exactly match an entry in `contractforge.execution.allowed-base-urls`
  (`http://localhost:8080` / `http://127.0.0.1:8080` by default). Anything else — HTTPS, a public host, a typo —
  is rejected with `400` before parsing, generation, or execution starts.
- **No LLM-chosen destinations.** The scenario payload is used only as the request body. The URL, HTTP method,
  and `X-Request-Id` header all come from the trusted `EndpointSnapshot` and the executor's own fixed logic —
  never from anything OpenAI returned.
- **Nothing sensitive is logged or returned.** Logs and the report contain status codes, durations, and
  generated request ids — never payloads, response bodies, or the API key.

## Why not just call the Phase 3 harness?

The Phase 3 harness (`executor/ContractForgeDynamicApiTest`, under `src/test/java`) is a **JUnit 5 Dynamic Test**
that only runs via `mvn test -Dcontractforge...` — deliberately outside the running Spring app, so nobody can
trigger a real API execution just by reaching ContractForge's HTTP port. `POST /api/pipeline/run` intentionally
reuses that same design (allowlist, trusted method/path, fresh request id, non-throwing per-scenario execution)
but reimplemented with Spring's `RestClient` (`service/ScenarioExecutionClient`) instead of RestAssured, because
RestAssured is a **test-scope-only** dependency and moving it into the main app would blur that boundary further
than necessary. The two executors are intentionally similar in behavior, not the same class.
