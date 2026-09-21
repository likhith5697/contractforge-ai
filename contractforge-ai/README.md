# contractforge-ai — Phase 1: Deterministic OpenAPI Contract Parser

**Phase 1 is deterministic parsing only — no LLM, no AI, no test generation.** It fetches an OpenAPI document,
resolves every schema reference, and produces a clean, predictable field map for each POST endpoint. Nothing in
this phase is generated or inferred by a model.

## What it does, in plain English

ContractForge reads the banking API's Swagger/OpenAPI document, finds every `POST` operation, follows every
`$ref` link in the request schemas (including nested objects and arrays), and turns each endpoint into a compact
list of addressable fields — with their type, whether they're required, enum values, min/max, patterns, and
examples. The result is cached in memory and served back over a small REST API. This snapshot is the input a
later phase will hand to an LLM to generate test scenarios — Phase 1 guarantees that input is accurate and
deterministic before any AI touches it.

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
```

## Running both applications

Nothing needs to be installed locally — a root-level `docker-compose.yml` (one directory up, alongside
`banking-api/`) builds and runs both services together on a shared network:

```bash
cd ..                     # the workspace root, containing both banking-api/ and contractforge-ai/
docker compose up --build
```

Compose starts `banking-api` first, waits for its healthcheck to pass, then starts `contractforge-ai` with
`CONTRACTFORGE_OPENAPI_SOURCE_URL` already pointed at `http://banking-api:8080/v3/api-docs` — the two containers
resolve each other by service name on Compose's internal network, so no `host.docker.internal` or manually
published ports are needed between them. Both are still published to the host: banking-api at `:8080`,
ContractForge at `:8081`.

To build/run this project on its own (e.g. against a banking-api already running elsewhere), use its own
Dockerfile directly:

```bash
cd contractforge-ai
docker build -t contractforge-ai:local .
docker run --rm -p 8081:8081 \
  -e CONTRACTFORGE_OPENAPI_SOURCE_URL=http://host.docker.internal:8080/v3/api-docs \
  contractforge-ai:local
```

(`host.docker.internal` is how a standalone container reaches services published on the host machine — only
needed when *not* using the combined root `docker-compose.yml` above.)

## Configuration

All settings are under `contractforge.openapi.*` in `application.yml`, overridable via environment variables:

| Property | Env var | Default |
|---|---|---|
| `contractforge.openapi.source-url` | `CONTRACTFORGE_OPENAPI_SOURCE_URL` | `http://localhost:8080/v3/api-docs` |
| `contractforge.openapi.connect-timeout-ms` | `CONTRACTFORGE_OPENAPI_CONNECT_TIMEOUT_MS` | `5000` |
| `contractforge.openapi.read-timeout-ms` | `CONTRACTFORGE_OPENAPI_READ_TIMEOUT_MS` | `10000` |

The source URL is never hard-coded anywhere in the parsing/service code — it's only ever read from
`OpenApiSourceProperties`.

## API

| Method & Path | Behavior |
|---|---|
| `POST /api/contracts/parse` | Fetches and parses the configured OpenAPI source; caches the result in memory; returns the full `ContractParseResult`. |
| `GET /api/contracts/snapshots` | Returns the cached `ContractParseResult`. Returns `409 Conflict` if `parse` hasn't been called yet. |
| `GET /api/contracts/snapshots/{encodedEndpointId}` | Returns one `EndpointSnapshot`. The id (e.g. `POST:/api/payments/scheduled`) is URL-safe Base64 encoded since it contains `:` and `/`. Returns `404` if unknown, `409` if nothing has been parsed yet. |

### Example curl commands

```bash
# 1. Parse the configured source (banking-api must be running at :8080)
curl -X POST http://localhost:8081/api/contracts/parse

# 2. List every discovered endpoint snapshot
curl http://localhost:8081/api/contracts/snapshots

# 3. Fetch one endpoint by its Base64url-encoded id
#    "POST:/api/payments/scheduled" -> UE9TVDovYXBpL3BheW1lbnRzL3NjaGVkdWxlZA
curl http://localhost:8081/api/contracts/snapshots/UE9TVDovYXBpL3BheW1lbnRzL3NjaGVkdWxlZA

# Encode any endpoint id yourself:
printf '%s' "POST:/api/payments/scheduled" | base64 | tr '+/' '-_' | tr -d '='
```

### Error response shape

Every failure returns a consistent body:

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
| `CONTRACT_NOT_PARSED` | 409 | Snapshots requested before any successful parse |
| `ENDPOINT_SNAPSHOT_NOT_FOUND` | 404 | Unknown endpoint id |

## Observability

- `GET /actuator/health` includes a `contractParse` component: `UNKNOWN` until the first successful parse in the
  application's lifetime, then `UP` with the source URL, timestamp, and endpoint count of the latest run.
- Logs report the source URL, the number of POST endpoints found, and the number of fields extracted per
  endpoint — never full request/response payloads or secrets.

## Sample response — `POST:/api/payments/scheduled` (shortened)

```json
{
  "endpointId": "POST:/api/payments/scheduled",
  "method": "POST",
  "path": "/api/payments/scheduled",
  "operationId": "schedulePayment",
  "summary": "Schedule a future or recurring bill payment",
  "tags": ["Payments"],
  "requiredHeaders": [],
  "requestSchemaName": "ScheduledPaymentRequest",
  "requestFields": [
    { "jsonPath": "accountId", "type": "string", "required": true, "example": "ACC-3D2E1F0A9B8C" },
    { "jsonPath": "amount.amount", "type": "number", "required": true, "minimum": 0.01 },
    { "jsonPath": "amount.currency", "type": "string", "required": true, "pattern": "^[A-Z]{3}$" },
    { "jsonPath": "billerCategory", "type": "string", "required": true,
      "enumValues": ["CREDIT_CARD", "ELECTRICITY", "INSURANCE", "INTERNET", "MOBILE_POSTPAID", "WATER"] },
    { "jsonPath": "notifyBeforeDays", "type": "integer", "required": false, "minimum": 0, "maximum": 30 },
    { "jsonPath": "scheduleDetails.frequency", "type": "string", "required": true,
      "enumValues": ["MONTHLY", "ONE_TIME", "QUARTERLY", "WEEKLY"] },
    { "jsonPath": "scheduleDetails.startDate", "type": "string", "format": "date", "required": true }
  ]
}
```

This is a real, live capture — obtained by running ContractForge against banking-api's actual `/v3/api-docs`.

## Testing

Tests never require banking-api to be running — a static fixture at
`src/test/resources/scheduled-payment-openapi-fixture.json` stands in for it.

```bash
./mvnw test
```

- `SchemaFlattenerTest` — unit tests against hand-built schema objects: nested object flattening, circular
  reference guarding, sibling reuse of the same schema (not a false cycle), array-of-objects bracket notation,
  enum/min/max extraction.
- `ContractParseServiceTest` — drives the full fetch → parse → flatten pipeline against the fixture: exactly one
  endpoint, correct `requestSchemaName`, flattened paths (`amount.currency`, `amount.value`,
  `scheduleDetails.frequency`), required-field recognition, enum values, min/max, only-required-headers
  filtering, and both fetch-failure and parse-failure error paths.
- `ContractParseControllerTest` — REST contract: `409` before any parse, `200` with the parsed result after,
  `404` for an unknown endpoint id.

## What's next (Phase 2)

Phase 2 will take these deterministic `EndpointSnapshot`s and hand them to an LLM to generate structured test
payload **scenarios** (valid, boundary, and invalid cases per field), which will then be validated back against
the same snapshots (required fields present, enums respected, patterns matched) before being handed to
RestAssured for execution. Phase 1's job ends at producing a trustworthy, unambiguous contract snapshot — no AI
is involved up to this point.
