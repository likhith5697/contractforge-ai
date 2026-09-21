# contractforge-ai

**Phase 1 — Deterministic OpenAPI Contract Parser**, **Phase 2 — OpenAI-Powered Scenario Generator**, and
**Phase 3 — Deterministic API Execution Harness**, for the ContractForge AI test-automation pipeline.

## What it does, in plain English

**Phase 1**: ContractForge reads the banking API's Swagger/OpenAPI document, finds every `POST` operation,
follows every `$ref` link in the request schemas (including nested objects and arrays), and turns each endpoint
into a compact list of addressable fields — with their type, whether they're required, enum values, min/max,
patterns, and documented response status codes.

**Phase 2**: ContractForge takes one of those trusted, already-parsed endpoint snapshots, asks OpenAI for
realistic synthetic test payloads for it, then checks every returned payload against the schema before showing
it. **The AI cannot call the banking API, write Java code, choose a target URL, or touch a file — it only returns
JSON scenario data, which is then deterministically validated.** Every successful generation also saves the
validated (accepted-only) scenarios as a **scenario artifact** JSON file on disk.

**Phase 3**: ContractForge does not generate Java test files. A **fixed** JUnit 5 Dynamic Test harness reads that
accepted-scenario JSON, turns each scenario into a runtime test, calls the approved Test API URL with it, and
deterministically compares the actual HTTP status against the expected one — writing a JSON execution report as
it goes. **The LLM creates only data; the fixed Java/RestAssured/JUnit harness performs the real API call and
determines pass or fail.**

## Architecture flow

```text
banking-api Swagger/OpenAPI JSON   (GET http://localhost:8080/v3/api-docs)
        │
        ▼
ContractForge fetcher               (OpenApiFetcher — RestClient with connect/read timeouts)
        │
        ▼
deterministic Swagger parser        (ContractParseService — io.swagger.parser.v3:swagger-parser-v3)
        │
        ▼
recursive $ref resolver             (SchemaFlattener — walks #/components/schemas/*, guards against cycles)
        │
        ▼
compact endpoint snapshots          (EndpointSnapshot + FieldSnapshot, cached in ContractSnapshotCache)
        │
        ▼  (Phase 2, on demand for one endpoint)
ContractForge loads that one trusted snapshot
        │
        ▼
snapshot + intent sent to OpenAI     (OpenAiResponsesClient — POST /v1/responses, structured JSON-schema output)
        │
        ▼
OpenAI returns strict JSON scenarios with complete payloads   (untrusted until validated)
        │
        ▼
ContractForge validates every scenario deterministically      (ScenarioValidator)
        │
        ▼
accepted-only scenarios saved as a scenario artifact           (ScenarioArtifact, on disk under target/contractforge/scenarios/)
        │
        ▼  (Phase 3, run separately via `mvn test -Dcontractforge...`)
fixed JUnit 5 Dynamic Test harness loads the artifact          (ContractForgeDynamicApiTest, no Spring context)
        │
        ▼
one real HTTP call per accepted scenario                       (ScenarioHttpExecutor — RestAssured, allow-listed target only)
        │
        ▼
actual vs expected status compared deterministically            (AssertionError with evidence on mismatch)
        │
        ▼
JSON execution report written incrementally                     (TestRunReport, under target/contractforge/reports/)
```

## Running both applications

Nothing needs to be installed locally — a root-level `docker-compose.yml` (one directory up, alongside
`banking-api/`) builds and runs both services together on a shared network:

```bash
cd ..                     # the workspace root, containing both banking-api/ and contractforge-ai/
docker compose up --build
```

Compose starts `banking-api` first (port `8080`), waits for its healthcheck, then starts `contractforge-ai`
(port `8081`) already pointed at `http://banking-api:8080/v3/api-docs` on the shared network.

To build/run this project on its own:

```bash
cd contractforge-ai
docker build -t contractforge-ai:local .
docker run --rm -p 8081:8081 \
  -e CONTRACTFORGE_OPENAPI_SOURCE_URL=http://host.docker.internal:8080/v3/api-docs \
  --env-file .env \
  contractforge-ai:local
```

## OpenAI API key setup (required for Phase 2 only — Phase 1 works without it)

Copy `.env.example` to `.env` and fill in your real key. **Never commit `.env` or paste a real key into
`application.yml`, tests, logs, or this README** — `.env` is already excluded via `.gitignore`/`.dockerignore`.

**macOS/Linux:**

```bash
cp .env.example .env
# then edit .env and set OPENAI_API_KEY=sk-...

# or, to set it for the current shell session only, without a file:
export OPENAI_API_KEY="sk-..."
export OPENAI_MODEL="gpt-5-mini"
```

**Windows PowerShell:**

```powershell
Copy-Item .env.example .env
# then edit .env and set OPENAI_API_KEY=sk-...

# or, to set it for the current session only, without a file:
$env:OPENAI_API_KEY = "sk-..."
$env:OPENAI_MODEL = "gpt-5-mini"
```

If `OPENAI_API_KEY` is absent, `POST /api/scenarios/generate` returns `503 Service Unavailable` immediately —
no call to OpenAI is ever attempted.

## End-to-end: parse → generate → execute

With Java 21 + Maven installed locally (or run the equivalent `mvn` inside a `maven:3.9-eclipse-temurin-21`
container as shown throughout this README if you'd rather not install anything):

```bash
# Terminal 1: start banking-api from its own project
cd banking-api
./mvnw spring-boot:run

# Terminal 2: start ContractForge
cd contractforge-ai
./mvnw spring-boot:run

# Parse the banking API contract
curl -X POST http://localhost:8081/api/contracts/parse

# Generate and validate scenarios; note the "artifactPath" in the response
curl -X POST http://localhost:8081/api/scenarios/generate \
  -H "Content-Type: application/json" \
  -d '{
    "endpointId": "POST:/api/payments/scheduled",
    "intent": "Generate one happy path, invalid enum, missing required field, and boundary scenarios",
    "maxScenarios": 4
  }'

# Terminal 3: execute the fixed Dynamic Test harness against the artifact just produced
cd contractforge-ai
./mvnw test \
  -Dcontractforge.scenario-artifact=target/contractforge/scenarios/<artifact-id>.json \
  -Dcontractforge.test-base-url=http://localhost:8080
```

Reports land in `target/contractforge/reports/`.

## Configuration

| Property | Env var | Default |
|---|---|---|
| `contractforge.openapi.source-url` | `CONTRACTFORGE_OPENAPI_SOURCE_URL` | `http://localhost:8080/v3/api-docs` |
| `contractforge.openapi.connect-timeout-ms` | `CONTRACTFORGE_OPENAPI_CONNECT_TIMEOUT_MS` | `5000` |
| `contractforge.openapi.read-timeout-ms` | `CONTRACTFORGE_OPENAPI_READ_TIMEOUT_MS` | `10000` |
| `contractforge.openai.base-url` | `CONTRACTFORGE_OPENAI_BASE_URL` | `https://api.openai.com/v1` |
| `contractforge.openai.api-key` | `OPENAI_API_KEY` | *(empty — required for Phase 2)* |
| `contractforge.openai.model` | `OPENAI_MODEL` | `gpt-5-mini` |
| `contractforge.openai.timeout-ms` | `CONTRACTFORGE_OPENAI_TIMEOUT_MS` | `30000` |
| `contractforge.openai.max-scenarios` | `CONTRACTFORGE_OPENAI_MAX_SCENARIOS` | `8` |
| `contractforge.artifacts.scenario-directory` | `CONTRACTFORGE_ARTIFACTS_SCENARIO_DIRECTORY` | `target/contractforge/scenarios` |
| `contractforge.artifacts.report-directory` | *(harness reads `contractforge.artifacts.report-directory` as a JVM system property — see Phase 3 below)* | `target/contractforge/reports` |
| `contractforge.execution.allowed-base-urls` | *(harness reads `contractforge.execution.allowed-base-urls` as a JVM system property)* | `http://localhost:8080,http://127.0.0.1:8080` |
| `contractforge.execution.connect-timeout-ms` | *(system property)* | `5000` |
| `contractforge.execution.read-timeout-ms` | *(system property)* | `15000` |

No URL, model name, or key is ever hard-coded in parsing/AI logic — everything is read from
`OpenApiSourceProperties` / `OpenAiProperties` / `ArtifactProperties` / `ExecutionProperties`. The Phase 3 harness
runs as a plain JUnit test with no Spring context, so it re-reads the same property names as JVM system
properties (`-Dcontractforge...`) with identical defaults, rather than injecting the Spring classes directly.

## API

| Method & Path | Behavior |
|---|---|
| `POST /api/contracts/parse` | Fetches and parses the configured OpenAPI source; caches the result in memory. |
| `GET /api/contracts/snapshots` | Returns the cached `ContractParseResult`. `409` if `parse` hasn't run yet. |
| `GET /api/contracts/snapshots/{encodedEndpointId}` | Returns one `EndpointSnapshot` (Base64url-encoded id, e.g. `POST:/api/payments/scheduled`). `404` if unknown, `409` if nothing parsed yet. |
| `POST /api/scenarios/generate` | Generates and validates synthetic payload scenarios for one already-parsed endpoint; persists a scenario artifact. See below. |
| `GET /api/scenarios/artifacts/{artifactId}` | Returns one previously saved `ScenarioArtifact` by id. `404` if unknown. Read-only. |

### `POST /api/scenarios/generate`

Request:

```json
{
  "endpointId": "POST:/api/payments/scheduled",
  "intent": "Generate one happy path, missing required field cases, invalid enum cases, numeric boundary cases, and one business-risk scenario",
  "maxScenarios": 6
}
```

- `endpointId` — required.
- `intent` — optional, max 500 characters.
- `maxScenarios` — optional, clamped to `[1, 8]`.
- `409` if the parser cache is empty, `404` if `endpointId` isn't a parsed endpoint, `503` if `OPENAI_API_KEY` is
  absent.

Response:

```json
{
  "endpointId": "POST:/api/payments/scheduled",
  "model": "gpt-5-mini",
  "acceptedScenarios": [ ],
  "rejectedScenarios": [ ],
  "acceptedCount": 0,
  "rejectedCount": 0,
  "generatedAt": "2026-09-21T04:34:43.568Z",
  "artifactId": "9072646e-00bf-4b2f-b5c6-a7b8ffbcd0ca",
  "artifactPath": "target/contractforge/scenarios/9072646e-00bf-4b2f-b5c6-a7b8ffbcd0ca.json"
}
```

Every scenario the model returns is run through `ScenarioValidator` before it appears in either list — a
malformed or contract-violating LLM payload is never silently fixed, only reported as rejected with the specific
reasons. **Every** successful generation call persists a `ScenarioArtifact` (even one with zero accepted
scenarios) — `acceptedScenarios` only, never the rejected ones — and the response tells you exactly where it
landed (`artifactPath`) and its id (`artifactId`, also fetchable via `GET /api/scenarios/artifacts/{artifactId}`).
That artifact file is the input to the Phase 3 execution harness below. Runtime artifacts and reports under
`target/contractforge/` are generated output, not source code — they're git-ignored.

### Example curl commands

```bash
# 1. Parse the banking-api contract (banking-api must be running at :8080)
curl -X POST http://localhost:8081/api/contracts/parse

# 2. Generate scenarios for the scheduled-payment endpoint
curl -X POST http://localhost:8081/api/scenarios/generate \
  -H "Content-Type: application/json" \
  -d '{
    "endpointId": "POST:/api/payments/scheduled",
    "intent": "Generate one happy path, missing required field cases, invalid enum cases, numeric boundary cases, and one business-risk scenario",
    "maxScenarios": 6
  }'
```

### Error response shape

```json
{
  "timestamp": "2026-09-21T00:09:04.274Z",
  "status": 409,
  "errorCode": "CONTRACT_NOT_PARSED",
  "message": "No contract has been parsed yet. Call POST /api/contracts/parse first.",
  "details": []
}
```

| errorCode | HTTP status | Cause |
|---|---|---|
| `OPENAPI_SOURCE_UNREACHABLE` | 502 | banking-api unreachable, timed out, or returned non-2xx |
| `OPENAPI_INVALID_CONTRACT` | 422 | Response body isn't a valid OpenAPI document |
| `CONTRACT_NOT_PARSED` | 409 | Snapshots/scenarios requested before any successful parse |
| `ENDPOINT_SNAPSHOT_NOT_FOUND` | 404 | Unknown endpoint id |
| `SCENARIO_ARTIFACT_NOT_FOUND` | 404 | Unknown artifact id |
| `INVALID_REQUEST` | 400 | Malformed path parameter (e.g. a non-UUID artifact id) |
| `VALIDATION_FAILED` | 400 | Request body failed bean validation (e.g. blank `endpointId`) |
| `LLM_NOT_CONFIGURED` | 503 | `OPENAI_API_KEY` is absent |
| `LLM_AUTH_FAILED` / `LLM_UPSTREAM_ERROR` | 502 | OpenAI rejected the key, or returned a server error |
| `LLM_RATE_LIMITED` | 429 | OpenAI rate limit exceeded |
| `LLM_TIMEOUT` | 504 | OpenAI didn't respond in time |
| `LLM_MALFORMED_RESPONSE` / `LLM_EMPTY_RESPONSE` / `LLM_REFUSED` | 502 | OpenAI's output couldn't be used |

## Observability

- `GET /actuator/health` includes a `contractParse` component: `UNKNOWN` until the first successful parse, then
  `UP` with the source URL, timestamp, and endpoint count.
- Logs report the source URL / endpoint id, POST endpoint and field counts, the model name, generation duration,
  and requested/accepted/rejected scenario counts — **never** API keys, Authorization headers, or full generated
  payloads.

## Sample response — `POST:/api/payments/scheduled` snapshot (Phase 1, shortened)

```json
{
  "endpointId": "POST:/api/payments/scheduled",
  "requestSchemaName": "ScheduledPaymentRequest",
  "successStatusCodes": [201],
  "documentedErrorStatusCodes": [],
  "requestFields": [
    { "jsonPath": "accountId", "type": "string", "required": true, "example": "ACC-3D2E1F0A9B8C" },
    { "jsonPath": "amount.amount", "type": "number", "required": true, "minimum": 0.01 },
    { "jsonPath": "amount.currency", "type": "string", "required": true, "pattern": "^[A-Z]{3}$" },
    { "jsonPath": "billerCategory", "type": "string", "required": true,
      "enumValues": ["CREDIT_CARD", "ELECTRICITY", "INSURANCE", "INTERNET", "MOBILE_POSTPAID", "WATER"] },
    { "jsonPath": "notifyBeforeDays", "type": "integer", "required": false, "minimum": 0, "maximum": 30 },
    { "jsonPath": "scheduleDetails.frequency", "type": "string", "required": true,
      "enumValues": ["MONTHLY", "ONE_TIME", "QUARTERLY", "WEEKLY"] }
  ]
}
```

This is a real, live capture from banking-api's actual `/v3/api-docs`. Note `documentedErrorStatusCodes` is
empty here — banking-api's controllers don't all declare `@ApiResponse` for 400/409, so a NEGATIVE or BOUNDARY
scenario cannot currently be generated for this exact live endpoint (Phase 2 does not paper over this - it's the
honest, actual OpenAPI contract). The test fixture used in the automated tests documents both `201` and `400` so
the full NEGATIVE/BOUNDARY validation path has real coverage.

## Testing

Tests never call OpenAI or require banking-api to be running — a fake `LlmScenarioClient` and the static fixture
at `src/test/resources/scheduled-payment-openapi-fixture.json` stand in for both.

```bash
./mvnw test
```

- `SchemaFlattenerTest` — nested object flattening, circular reference guarding, array bracket notation,
  enum/min/max extraction.
- `ContractParseServiceTest` — full fetch → parse → flatten pipeline, required/enum/min-max recognition, only
  required headers, fetch/parse error paths.
- `ContractParseControllerTest` — REST contract for the contracts endpoints.
- `ScenarioGenerationServiceTest` — drives the real `ScenarioValidator` against a fake LLM response: 409 empty
  cache, 404 unknown endpoint, 503 missing API key, a valid happy-path + valid negative scenario both accepted,
  an unknown payload field rejected, a missing required field rejected, an invalid enum rejected, an undocumented
  `expectedStatus` rejected, and a simulated malformed-LLM-response exception propagating cleanly.
- `ScenarioControllerTest` — HTTP-level mapping for request validation (400), missing API key (503), and an LLM
  rate-limit failure (429).

## Phase 3 — executing accepted scenarios for real

ContractForge does not generate Java test files. A **fixed** JUnit 5 Dynamic Test harness
(`src/test/java/com/likhith/contractforge/executor/`) reads accepted-scenario JSON, turns each scenario into a
runtime test, calls the approved Test API URL, and deterministically compares actual versus expected status. No
LLM is involved in this step at all — the AI's job ended at Phase 2, producing validated *data*; this harness is
fixed Java code that decides pass/fail by comparing two integers.

### How it works, in plain English

For every accepted scenario in the artifact, the harness builds one JUnit "dynamic test." Running that test does
exactly this, and nothing else:
1. Combine the approved base URL (`http://localhost:8080` by default — never anything else unless explicitly
   allow-listed) with the endpoint path recorded in the artifact (never anything the payload could redirect to).
2. Send the scenario's payload as the request body, with a fresh `X-Request-Id` header and `Content-Type:
   application/json` — nothing else about the request is configurable by the scenario itself.
3. Compare the real HTTP response status to the scenario's `expectedStatus`.
4. Write (or update) a JSON report with the result, then fail the test with a clear message if the statuses
   didn't match.

The harness is generic: it contains no banking endpoint or field names, and would work unmodified against a
scenario artifact for any endpoint.

### Safety

- **Allowlist only.** `contractforge.test-base-url` (and any override of `contractforge.execution.allowed-base-urls`)
  must exactly match an entry in the allowlist (`http://localhost:8080` / `http://127.0.0.1:8080` by default) or
  the harness refuses to run — before any HTTP call is made. HTTPS and any other host are rejected by
  construction, since the allowlist only ever contains local `http://` entries.
- The scenario payload can **never** override the URL, method, or headers — those come only from the trusted
  artifact and the executor's own fixed logic.
- Logs and the JSON report never include the payload, response body, or any secret — only status codes,
  durations, and generated request ids.

### Running it

```bash
# Normal `mvn test` never calls out to a network - the harness returns an empty stream
# and is skipped, because contractforge.scenario-artifact isn't set.
./mvnw test

# To actually execute an artifact's accepted scenarios against a running Test API:
./mvnw test \
  -Dtest=ContractForgeDynamicApiTest \
  -Dcontractforge.scenario-artifact=target/contractforge/scenarios/<artifact-id>.json \
  -Dcontractforge.test-base-url=http://localhost:8080
```

### Execution report

Written to `target/contractforge/reports/<artifactId>-<timestamp>.json`, updated after every scenario (so a run
that fails partway still leaves a complete record of everything that ran before it):

```json
{
  "artifactId": "11111111-2222-3333-4444-555555555555",
  "endpointId": "POST:/api/payments/scheduled",
  "targetBaseUrl": "http://localhost:8080",
  "startedAt": "2026-09-21T05:12:25.929085878Z",
  "completedAt": "2026-09-21T05:12:34.908328608Z",
  "total": 2,
  "passed": 1,
  "failed": 1,
  "results": [
    {
      "scenarioName": "valid_monthly_electricity_payment",
      "expectedStatus": 201,
      "actualStatus": 201,
      "passed": true,
      "requestId": "7b08dfa3-b0fc-4f7d-ab3e-1679a7f568d6",
      "durationMs": 8635
    },
    {
      "scenarioName": "documented_but_wrong_status",
      "expectedStatus": 200,
      "actualStatus": 201,
      "passed": false,
      "requestId": "c3532023-c930-46a7-83b7-22d6018d99e2",
      "durationMs": 50
    }
  ]
}
```

This is a real, live capture: the first scenario is a genuinely valid scheduled payment (banking-api actually
returns `201`); the second deliberately claims `expectedStatus: 200` to demonstrate a real failing assertion —
mirroring the exact "springdoc defaults to 200 unless told otherwise" gap called out in Phase 1.

### Testing

```bash
./mvnw test
```

- `ScenarioHttpExecutorTest` — against a real local JDK `HttpServer` (no banking-api needed): sends the correct
  method/path/body/`X-Request-Id`, a matching status produces a passed result, a mismatched status throws an
  `AssertionError` with full evidence while still reporting the result, and a non-allow-listed base URL is
  rejected in the constructor before any request is sent.
- `ContractForgeDynamicApiTestTest` — end to end against the same local server: an absent
  `contractforge.scenario-artifact` produces an empty test stream; a two-scenario artifact produces two dynamic
  tests, executed in order, that write a report with correct `total`/`passed`/`failed` counts and never leak the
  payload value into the report file.
- `ScenarioGenerationServiceTest` — extended with "an artifact is persisted after generation" and "the artifact
  excludes rejected scenarios."
