# ContractForge AI

**In one sentence:** this project has a fake bank you can safely test against, and an AI-powered tool that
automatically figures out how to test it — writes realistic test cases, checks the AI's work before trusting it,
runs the tests for real, and tells you what passed and what didn't.

Nothing here touches a real bank, a real person's data, or a real payment. Everything is synthetic, and the AI
is never allowed to actually call an API or decide whether a test passed — it only ever proposes data.

---

## The problem this solves

Normally, testing an API means a person reads the API's documentation, thinks up test cases (a valid request,
a request missing a required field, a request with a bad value, an edge case), writes those test cases by hand,
and runs them. That's slow, and it's easy to miss cases.

This project automates that whole loop — while keeping a human-designed safety net around the one part that's
genuinely risky to automate: **trusting an AI's output**.

## The two pieces

| Project | What it is |
|---|---|
| **`banking-api/`** | A realistic *mock* bank: 20 API endpoints for things like opening an account, transferring money, applying for a card or a loan, filing a fraud report, etc. It's the "system under test" — a safe practice target, not a real bank. |
| **`contractforge-ai/`** | The testing tool. It reads `banking-api`'s own documentation, asks an AI to invent test scenarios, double-checks every one of those scenarios against the rules before trusting it, and can then actually run the accepted ones against `banking-api` and report the results. |

## How it works, start to finish

```
1. banking-api describes itself
   Every real API can describe its own shape in a standard format (OpenAPI/Swagger) — what endpoints
   exist, what fields each one needs, which ones are required, what values are allowed, etc.
   banking-api publishes this description automatically; nobody writes it by hand.

2. ContractForge reads that description
   It fetches banking-api's self-description and turns every endpoint into a clean, structured
   checklist of fields and rules — deterministically, with no AI involved at this stage.

3. ContractForge asks an AI for test data
   For one endpoint at a time, it hands the AI that checklist (never the whole codebase, never real
   data) and asks for realistic test payloads: a valid one, one missing a required field, one with
   a bad value, a boundary case, and so on.

4. ContractForge checks the AI's homework
   Every single value the AI returns is checked against the real checklist from step 2 — right field
   names, right types, right allowed values, nothing invented. Anything that doesn't hold up is
   thrown out and labeled "rejected," with the specific reason. Only what survives this check is
   ever used. The AI never gets to just be trusted.

5. ContractForge runs the accepted tests for real
   The surviving test cases get sent as real HTTP requests to banking-api, and the actual response
   is compared to what was expected. The AI has no say in where these requests go, what method they
   use, or what "pass" means — that's all fixed, ordinary code.

6. You get a report
   Pass/fail per test case, with enough detail to see exactly why anything failed.
```

You can run steps 3–6 one endpoint at a time, or trigger **one single API call** that does all of it — parses,
generates, validates, and executes — across every endpoint banking-api has, and hands back one combined report.

## Why the AI is kept on a short leash

This is the part worth understanding even if you skip everything else. The AI in this system:

- **Cannot** call any API itself.
- **Cannot** decide what counts as a pass or a fail.
- **Cannot** invent a field, a URL, or a status code that isn't already documented.
- **Can only** propose synthetic JSON data, which is then checked by ordinary, predictable, non-AI code before
  anything happens with it.

If the AI's response is malformed, inconsistent, or breaks a rule it wasn't supposed to, that specific test case
is simply thrown away and reported as rejected — it never silently gets "fixed up" or waved through.

## Running it

Requires only [Docker](https://www.docker.com/) — no Java, Maven, or anything else installed locally.

```bash
cd "ContractForge AI"
docker compose up -d --build
```

This starts both services:
- **banking-api** at `http://localhost:8080` (the fake bank, with a Swagger UI you can click through at
  `http://localhost:8080/swagger-ui/index.html`)
- **contractforge-ai** at `http://localhost:8081` (the testing tool)

To let it actually talk to an AI (needed for steps 3 onward above — steps 1–2 work without this):

```bash
cp contractforge-ai/.env.example contractforge-ai/.env
# edit contractforge-ai/.env and set your OpenAI API key
docker compose up -d --build contractforge-ai
```

Then, the simplest possible thing to try:

```bash
# Ask ContractForge to do everything, for every endpoint, in one call:
curl -X POST http://localhost:8081/api/pipeline/run
```

## Where to go for more detail

- **[banking-api/README.md](banking-api/README.md)** — every one of the 20 mock endpoints, example requests,
  how to explore it in Swagger UI.
- **[contractforge-ai/README.md](contractforge-ai/README.md)** — the parsing, generation, and execution phases
  in technical detail, configuration options, error codes, and the full test suite.
- **[contractforge-ai/PIPELINE.md](contractforge-ai/PIPELINE.md)** — the single "run everything" endpoint:
  exact request/response shape, safety rules, and what a real report looks like.

## A note on realistic limitations

The AI only ever sees an endpoint's declared *schema* — it has no way to know that a specific account number
or customer ID actually exists in banking-api's database. So a freshly-started banking-api with no data in it
will correctly reject many "valid-looking" AI-generated requests (e.g. "no such account"). That's not a bug —
it's the tool honestly reporting what it found, rather than pretending everything works.
