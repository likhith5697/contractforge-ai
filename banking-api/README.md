# banking-api

A realistic **mock banking-platform API suite** built with Spring Boot 3 / Java 21, designed to be exercised by the
**ContractForge AI** API-test-automation framework. All data, account numbers, and identifiers are **synthetic** —
no real PII, no real financial data.

- 20 domain-organized `POST` endpoints (customers, accounts, transfers, payments, cards, loans, KYC, fraud,
  disputes, notifications, consents, statements, merchant payments)
- Layered architecture: `controller` → `service` → `repository` → `entity`, with `dto/request` and `dto/response`
  per endpoint and a shared `@RestControllerAdvice` for consistent error handling
- H2 in-memory database (no external DB required)
- OpenAPI 3 / Swagger UI generated from the actual controllers and DTOs
- Runs entirely via **Docker** — no local JDK/Maven install required

---

## 1. Running it

### Option A — Docker Compose (recommended, nothing to install locally)

```bash
cd banking-api
docker compose up --build
```

The API will be available at `http://localhost:8080`. Stop it with `docker compose down`.

> Running banking-api together with the sibling `contractforge-ai` project? Use the root-level
> `docker-compose.yml` one directory up instead — it builds and starts both services on a shared network so
> ContractForge can reach this API at `http://banking-api:8080` without any extra configuration:
> `cd .. && docker compose up --build`.

### Option B — Run tests only, via a throwaway Maven container

```bash
cd banking-api
docker run --rm -v "$(pwd):/app" -w /app maven:3.9-eclipse-temurin-21 mvn test
```

### Option C — Local Maven wrapper (if you already have JDK 21 installed)

```bash
cd banking-api
./mvnw test
./mvnw spring-boot:run
```

### Useful links once running

| Resource | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON (for ContractForge AI contract parsing) | http://localhost:8080/v3/api-docs |
| Health check | http://localhost:8080/actuator/health |
| H2 console (dev only) | http://localhost:8080/h2-console (JDBC URL `jdbc:h2:mem:bankingdb`, user `sa`, empty password) |

---

## 2. Response envelope

Every endpoint returns a consistent envelope, and every response (success or error) carries the correlation id from
the incoming `X-Request-Id` header, or a generated UUID if none was supplied.

**Success (2xx):**
```json
{
  "requestId": "3f2b1a4c-7e21-4b9a-9c3d-1a2b3c4d5e6f",
  "status": "CREATED",
  "message": "Customer onboarded successfully",
  "data": { "...": "..." },
  "timestamp": "2026-04-01T10:15:30.123Z"
}
```

**Error (4xx/5xx):**
```json
{
  "requestId": "3f2b1a4c-7e21-4b9a-9c3d-1a2b3c4d5e6f",
  "status": "BAD_REQUEST",
  "errorCode": "VALIDATION_FAILED",
  "message": "Request validation failed",
  "fieldErrors": [
    { "field": "contactInfo.email", "message": "must be a well-formed email address" }
  ],
  "timestamp": "2026-04-01T10:15:30.123Z"
}
```

### Status code conventions

| Scenario | Status | errorCode examples |
|---|---|---|
| Successful create | `201 Created` | — |
| Bean validation failure (`@Valid`) | `400 Bad Request` | `VALIDATION_FAILED` |
| Business rule failure (e.g. same source/destination account, insufficient funds, referenced entity missing) | `400 Bad Request` | `SAME_ACCOUNT_TRANSFER`, `INSUFFICIENT_FUNDS`, `CUSTOMER_NOT_FOUND`, ... |
| Path-referenced resource missing (e.g. unknown `cardId`, `loanId`) | `404 Not Found` | `CARD_NOT_FOUND`, `LOAN_NOT_FOUND`, ... |
| Replayed `Idempotency-Key` on a transactional endpoint | `409 Conflict` | `DUPLICATE_IDEMPOTENCY_KEY` |

### Idempotency

Transactional/financial endpoints (transfers, payments, card application, loan application, KYC, disputes,
merchant payments) accept an optional `Idempotency-Key` request header. Reusing the same key on the same endpoint
returns `409 Conflict` without re-running the operation.

---

## 3. Endpoint catalogue

| # | Method & Path | Purpose |
|---|---|---|
| 1 | `POST /api/customers` | Customer onboarding |
| 2 | `POST /api/customers/{customerId}/addresses` | Add address to a customer |
| 3 | `POST /api/accounts` | Open a bank account |
| 4 | `POST /api/accounts/{accountId}/beneficiaries` | Add a beneficiary to an account |
| 5 | `POST /api/transfers` | Domestic funds transfer |
| 6 | `POST /api/transfers/international` | International wire (SWIFT) transfer |
| 7 | `POST /api/payments` | Immediate bill payment |
| 8 | `POST /api/payments/scheduled` | Schedule a future/recurring bill payment |
| 9 | `POST /api/cards` | Card application |
| 10 | `POST /api/cards/{cardId}/activate` | Activate an issued card |
| 11 | `POST /api/cards/{cardId}/limits` | Change a card's spending limit |
| 12 | `POST /api/loans/applications` | Submit a loan application |
| 13 | `POST /api/loans/{loanId}/documents` | Register document metadata (no binary upload) |
| 14 | `POST /api/kyc/verifications` | Record a KYC verification |
| 15 | `POST /api/fraud/cases` | Open a fraud investigation case |
| 16 | `POST /api/disputes` | File a transaction dispute |
| 17 | `POST /api/notifications/preferences` | Set notification preferences |
| 18 | `POST /api/consents` | Record a customer consent decision |
| 19 | `POST /api/statements/requests` | Request an account statement |
| 20 | `POST /api/merchant-payments` | Merchant (POS/online) payment |

Full request/response schemas (including nested objects, enums, and constraints) are in the OpenAPI spec at
`/v3/api-docs`, or interactively in Swagger UI.

---

## 4. Example curl commands

### Onboard a customer

```bash
curl -s -X POST http://localhost:8080/api/customers \
  -H "Content-Type: application/json" \
  -d '{
    "personalInfo": {"firstName":"Aarav","lastName":"Sharma","dateOfBirth":"1990-05-14","gender":"MALE","nationality":"IN"},
    "contactInfo": {"email":"aarav.sharma@example-mail.com","phoneNumber":"+14155552671"},
    "identityDocument": {"documentType":"PASSPORT","documentNumber":"SYN-DOC-8842190","issuingCountry":"IN","expiryDate":"2030-01-01"},
    "employmentInfo": {"occupation":"Software Engineer","employmentType":"SALARIED","annualIncome":95000.00},
    "residentialAddress": {"addressLine1":"221B Synthetic Lane","city":"Springfield","postalCode":"62704","country":"US"},
    "riskProfile": {"sourceOfFunds":"SALARY","expectedMonthlyTurnover":5000.00}
  }'
```

### Open an account (use the `customerId` from the response above)

```bash
curl -s -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUS-XXXXXXXXXXXX",
    "accountType": "SAVINGS",
    "currency": "USD",
    "initialDeposit": 5000.00,
    "branchCode": "BR-0451",
    "purpose": "PERSONAL",
    "nomineeDetails": {"nomineeName":"Priya Sharma","relationship":"SPOUSE","sharePercentage":100},
    "channelMetadata": {"channel":"WEB","deviceId":"DEV-9F1A2B3C","ipAddress":"203.0.113.42"}
  }'
```

### Domestic transfer (with idempotency key)

```bash
curl -s -X POST http://localhost:8080/api/transfers \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: 3d9f7c1e-2b4a-4e6d-9c1a-1234567890ab" \
  -d '{
    "sourceAccountId": "ACC-XXXXXXXXXXXX",
    "destinationAccountId": "ACC-YYYYYYYYYYYY",
    "amount": {"amount": 250.00, "currency": "USD"},
    "transferType": "IMPS",
    "purposeCode": "GOODS_SERVICES",
    "remarks": "Invoice #4471",
    "channelMetadata": {"channel":"MOBILE","deviceId":"DEV-1122AABB","ipAddress":"203.0.113.50"}
  }'
```

### International wire transfer

```bash
curl -s -X POST http://localhost:8080/api/transfers/international \
  -H "Content-Type: application/json" \
  -d '{
    "sourceAccountId": "ACC-XXXXXXXXXXXX",
    "beneficiary": {
      "beneficiaryName": "Global Trading Ltd",
      "beneficiaryAddress": {"addressLine1":"10 Downing Street","city":"London","country":"GB"},
      "bankDetails": {"bankName":"Global Trust Bank Plc","swiftBic":"GTBPGB2LXXX","iban":"GB29NWBK60161331926819","bankCountry":"GB"}
    },
    "amount": {"amount": 1200.00, "currency": "USD"},
    "purposeOfTransfer": "TRADE",
    "chargeOption": "SHA",
    "complianceMetadata": {"sanctionsScreeningConsent": true, "sourceOfFundsDeclaration": "Business trade proceeds"}
  }'
```

### Card application

```bash
curl -s -X POST http://localhost:8080/api/cards \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUS-XXXXXXXXXXXX",
    "accountId": "ACC-XXXXXXXXXXXX",
    "cardType": "CREDIT",
    "cardNetwork": "VISA",
    "cardholderName": "AARAV SHARMA",
    "isVirtualCard": false,
    "requestedCreditLimit": 10000.00,
    "billingCycle": 5,
    "deliveryAddress": {"addressLine1":"742 Evergreen Terrace","city":"Springfield","postalCode":"62704","country":"US"}
  }'
```

### Loan application

```bash
curl -s -X POST http://localhost:8080/api/loans/applications \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "CUS-XXXXXXXXXXXX",
    "loanType": "PERSONAL",
    "requestedAmount": {"amount": 15000.00, "currency": "USD"},
    "tenureMonths": 36,
    "purpose": "Home renovation",
    "employmentDetails": {"employmentType":"SALARIED","monthlyIncome":7500.00,"employerName":"Synthetic Tech Corp"},
    "collateral": {"collateralType": "NONE"}
  }'
```

### Merchant payment

```bash
curl -s -X POST http://localhost:8080/api/merchant-payments \
  -H "Content-Type: application/json" \
  -d '{
    "accountId": "ACC-XXXXXXXXXXXX",
    "merchantId": "MER-771234",
    "merchantName": "Synthetic Coffee Co.",
    "merchantCategory": "FOOD_AND_DINING",
    "amount": {"amount": 12.50, "currency": "USD"},
    "tipAmount": 2.00,
    "paymentInstrument": "DEBIT_CARD",
    "items": [{"itemName":"Cappuccino","quantity":2,"unitPrice":4.75}],
    "deviceMetadata": {"channel":"MOBILE","deviceId":"DEV-1122AABB","ipAddress":"203.0.113.50"}
  }'
```

More payload examples (KYC, fraud, disputes, notification preferences, consents, statements, scheduled payments,
beneficiaries, card activation/limits, loan documents) are available directly in Swagger UI with realistic
pre-filled examples on every field.

---

## 5. Testing

```bash
docker run --rm -v "$(pwd):/app" -w /app maven:3.9-eclipse-temurin-21 mvn test
```

`BankingApiIntegrationTest` uses `MockMvc` against the full Spring context (H2 in-memory) and covers:

1. Valid customer creation → `201`
2. Invalid customer email → `400` with a `contactInfo.email` field error
3. Valid account creation for an existing customer → `201`
4. Negative transfer amount → `400`
5. Same source and destination account on a transfer → `400`
6. Duplicate `Idempotency-Key` on a transfer → `409`
7. Scheduled payment with a past `startDate` → `400`

---

## 6. Project layout

```
banking-api/
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── src
    ├── main
    │   ├── java/com/likhith/bankingapi
    │   │   ├── BankingApiApplication.java
    │   │   ├── common/            # ApiResponse, ApiErrorResponse, IdGenerator, RequestIdHolder
    │   │   ├── config/            # OpenApiConfig, RequestIdFilter, CardLimitsProperties
    │   │   ├── controller/        # 12 domain controllers covering the 20 endpoints
    │   │   ├── dto/request/       # one request DTO (+ nested types) per endpoint
    │   │   ├── dto/response/      # one response DTO per endpoint
    │   │   ├── entity/            # JPA entities + entity/enums
    │   │   ├── exception/         # GlobalExceptionHandler + typed exceptions
    │   │   ├── repository/        # Spring Data JPA repositories
    │   │   └── service/           # business logic, one service per domain
    │   └── resources/application.yml
    └── test/java/com/likhith/bankingapi/BankingApiIntegrationTest.java
```

## 7. Notes for ContractForge AI

- The OpenAPI document at `/v3/api-docs` is generated live from the actual `@RestController`/DTO code — every one
  of the 20 endpoints, their request/response schemas, enums, and validation constraints (`@NotBlank`, `@Pattern`,
  `@DecimalMin`, etc.) are reflected there for contract parsing.
- All generated identifiers follow predictable prefixes: `CUS-`, `ADR-`, `ACC-`, `BEN-`, `TRF-`, `ITX-`, `PAY-`,
  `SPM-`, `CARD-`, `CLC-`, `LOAN-`, `DOC-`, `KYC-`, `FRD-`, `DSP-`, `NPF-`, `CNS-`, `STR-`, `MPY-`.
- The H2 database resets on every application restart — seed your own fixtures (customers/accounts) at the start
  of a test run via `POST /api/customers` and `POST /api/accounts` before exercising dependent endpoints.
