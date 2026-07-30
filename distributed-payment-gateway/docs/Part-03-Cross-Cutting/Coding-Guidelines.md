# Coding-Guidelines.md — Platform-Wide Coding Guidelines

Consolidates and extends `02_ENGINEERING_STANDARDS.md` with the concrete patterns already applied consistently across every per-service specification (`Merchant-Service.md`, `Token-Vault.md`, `Payment-Orchestrator.md`, `Acquiring-Adapter.md`, `Webhook-Service.md`, `Settlement-Service.md`). This document does not introduce new rules beyond what those specs already establish — it is the single reference an engineer implementing any service should read first.

No code examples are included here by design — these are structural and naming rules, not syntax to copy.

---

# 1. Project Structure

- One Maven module per service, all sharing the platform parent POM and `platform/common-*` libraries (`SYSTEM_DESIGN.md` §13).
- Every service's internal package layout follows the same top-level shape: `config/`, `controller/`, `application/`, `domain/`, `port/`, `adapter/`, `entity/`, `dto/`, `mapper/`, `exception/`, `security/`, `validation/`, `event/`, `scheduler/`, `client/`, `constant/`.
- Service-specific cross-cutting concerns get their own top-level package rather than being buried inside `application/` — e.g. Payment Orchestrator's `saga/` and `routing/`, Token Vault's `adapter/hsmkms/`, Webhook Service's `signing/` and `retry/`, Settlement Service's `scheduling/`, `batching/`, `calculation/`. This is a deliberate, repeated deviation from a rigid one-size-fits-all layout: a component invoked by multiple use cases belongs in its own package, never duplicated inside each calling use case.
- API Gateway is the one structural exception — no `service/`, `controller/`, `repository/`, or `entity/` packages at all, since it has no persistence layer and is a routing/filter-chain component, not a resource server (`API-Gateway-Part-01.md` §12). This exception is documented, not accidental.

---

# 2. Naming Conventions

| Element | Convention | Avoid |
|---|---|---|
| Classes | `MerchantController`, `PaymentService`, `TokenizePanUseCase`, `CredentialIssuancePolicy` | `Helper`, `Manager` (except where the platform's own established pattern uses it deliberately, e.g. `KeyManager`, `RetryManager` — a small, intentional exception list, not a loophole), `CommonUtil`, `Temp` |
| Methods | `createMerchant()`, `authorizePayment()`, `detokenize()`, `capturePayment()` | `process()`, `handle()`, `execute()` |
| Domain events | Past-tense, entity-first: `MerchantSuspended`, `TokenRevoked`, `PaymentCaptured` | Imperative/command-style names (`SuspendMerchant`) |
| DTOs | `{Verb}{Entity}Request` / `{Entity}Response` | Reusing a domain/entity class as a DTO |
| Value objects | Noun describing the concept, not its storage shape: `MaskedPan`, `VaultTokenId`, `LedgerEntryReference` | Suffixing every value object with `VO` or similar decorative noise |
| Sealed types for finite state | `PaymentState`, `TokenStatus`, `KeyStatus` — always `sealed`, per Java 21 | An `enum` where transition-specific behavior is needed, or a plain `String` status field anywhere |

---

# 3. Layer Responsibilities

| Layer | Owns | Never Owns |
|---|---|---|
| Presentation/Controller | Request/response mapping, structural delegation to a use case | Business logic, direct repository access |
| Application (Use Case) | Orchestration: load aggregate, invoke domain method, persist via repository port | Business rules themselves (those live in the domain layer) |
| Domain | Aggregates, entities, value objects, domain events, domain services, specifications, invariants | Any framework annotation, any persistence-technology or HTTP-client dependency |
| Port | Interfaces only — the contract between application and infrastructure | Any implementation detail |
| Adapter/Infrastructure | Concrete repository implementations, external client implementations, Outbox writer, framework wiring | Business rules — an adapter should be swappable without touching domain logic |

This is the same Clean Architecture layering applied identically across all seven components; the only variation is which concrete adapters exist per service (e.g. Token Vault's HSM/KMS adapter, Acquiring Adapter's per-provider connectors).

---

# 4. DTO Rules

- Never expose an entity or domain aggregate directly in a controller response — always map to a dedicated Response DTO.
- Request and Response DTOs are always separate types, even when their fields overlap substantially.
- DTOs are immutable (Java 21 `record` types) — no builder-pattern mutation, no setter-based partial construction.
- A field that must never be echoed back (a raw secret, a full tax identifier, cardholder data) simply has no corresponding field in the Response DTO's type signature — this is a structural guarantee, not a runtime `if` check that could be forgotten.
- One-time-disclosure fields (credential secrets, webhook signing secrets) exist only on the specific creation-response DTO type, never on the general-purpose "get this resource" DTO.

---

# 5. Entity Rules

- Persistence concerns only — no business logic on a JPA/R2DBC-mapped entity class.
- Entities are distinct types from domain aggregates; a domain aggregate (`Token`, `Payment`, `Merchant`) is mapped to/from its corresponding persistence entity by a repository adapter, never conflated into one class serving both roles.
- UUID identifiers everywhere, never sequential integers — established first for enumeration-resistance in Token Vault (`Token-Vault-Part-03.md` §21.3) and applied platform-wide.
- Optimistic locking (`version` column) on every mutable aggregate root, checked-and-incremented on every update.
- Append-only tables (ledger entries, audit entries, delivery attempts) map to entities with no `UPDATE`/`DELETE` code path at all — enforced at the repository-adapter level, and reinforced by a database-level permission restriction wherever the isolation requirement warrants it (Token Vault's audit database).

---

# 6. Exception Handling

- Centralized exception handling per service — a single global exception handler maps domain/application exceptions to the platform-standard error envelope (`API-Standards.md` §7).
- Domain-specific exceptions, not generic `RuntimeException` throws — a rejected state transition, an authorization denial, and a validation failure are each their own exception type, mapped to their own documented error code.
- Never expose a stack trace, an internal exception message, or an internal identifier in any response body — the mapping from internal exception to external error code is always deliberate, never a pass-through of `exception.getMessage()`.
- A security-relevant rejection (authentication/authorization failure) is deliberately generic in its external message, even though the internal exception/log carries the specific reason — this prevents an attacker from learning why a request was denied.
- Retry-eligible vs terminal failures are distinguished by exception type, not by a caller re-inspecting an HTTP status code after the fact — the exception type itself encodes whether a caller's retry logic should engage.

---

# 7. Logging Rules

- Structured JSON only, platform-standard baseline fields (`Observability.md` §2).
- Never log: Authorization headers, API keys, JWTs, request/response bodies with sensitive fields, cardholder data, key material, webhook secrets — zero exceptions, platform-wide.
- Log severity reflects actionability, not just HTTP status: a `409` business-state rejection is `INFO`/`WARN`, not `ERROR`; a genuine system failure is always `ERROR`.
- A type that could ever hold sensitive data has no logging-framework-visible `toString()`/serialization path capable of leaking it — this is a structural guarantee applied most strictly in Token Vault, and a pattern every other service's sensitive-field types should follow identically.
- Every state transition and lifecycle-relevant event is logged unconditionally at `INFO`, never sampled away — these are exactly the log lines an operator needs to reconstruct an incident.

---

# 8. Testing Rules

- Domain layer: unit tests only, plain Java, no Spring context, no real database/HSM/Kafka — fake port implementations.
- Application layer: integration tests against real infrastructure via Testcontainers (PostgreSQL, Redis, Kafka) plus mocked external dependencies.
- Every lifecycle state machine (payment states, token states, merchant states, settlement states) requires 100% branch coverage on its transition logic — an untested invalid-transition path is treated as a correctness defect, not a coverage nitpick.
- Contract tests pin every cross-service API and event-payload shape against the actual consuming service's expectations — a producer never assumes a consumer's tolerance for a schema change without a passing contract test proving it.
- Concurrency-sensitive guarantees (idempotency deduplication, optimistic-lock conflict handling, duplicate-batch prevention) are tested with genuine concurrent load in the test itself, not asserted from single-threaded reasoning alone.
- Chaos/fault-injection testing verifies every documented failure-behavior claim (circuit-breaker opening, fail-closed vs graceful-degradation per dependency) actually holds under real fault injection, not just as a design intention in a spec.

---

# 9. Package Structure

Full per-service package trees are documented individually in each service's own specification (cross-referenced below) — this section states the shared rule, not the full listing:

| Service | Package Structure Reference |
|---|---|
| API Gateway | `API-Gateway-Part-01.md` §12 |
| Merchant Service | `Merchant-Service-Part-01.md` §29 |
| Token Vault | `Token-Vault-Part-01.md` §12 |
| Payment Orchestrator | `Payment-Orchestrator-Package-Structure.md` |
| Acquiring Adapter | `Acquiring-Adapter-Package-Structure.md` |
| Webhook Service | `Webhook-Service-Package-Structure.md` |
| Settlement Service | `Settlement-Service-Package-Structure.md` |

Every one of these follows the shared top-level shape from §1, with each service's own cross-cutting orchestration components broken out into their own package rather than nested under `application/`.

---

# 10. Clean Architecture Rules

- Dependencies point inward, always: `controller`/`adapter` → `application` → `domain`; `domain` depends on nothing outside itself, not even a logging framework's annotation.
- `port` interfaces are owned by the layer that needs them (application), implemented by the layer that provides them (adapter) — never the reverse.
- A domain aggregate never imports a Spring annotation, a JPA annotation, an HTTP client type, or a specific vendor SDK (KMS client, banking-system client) — this is what makes every service's core business logic testable without any of its real infrastructure running.
- Cross-aggregate invariants (e.g. "a credential can only be issued to an `ACTIVE` merchant") are enforced by a domain service, not by one aggregate reaching into another aggregate's internals directly.
- A vendor/provider integration change (new KMS provider, new acquirer, new banking system) touches only the adapter layer — if a domain or application class needs to change for a vendor swap, that's a Clean Architecture violation worth flagging in review.

---

# 11. Do's and Don'ts Table

| Do | Don't |
|---|---|
| Model lifecycle states as `sealed` types with explicit transition methods on the aggregate | Expose a raw mutable `status` field/setter on any aggregate |
| Enforce invariants inside the aggregate/domain service | Scatter the same validation check across multiple use cases hoping every call site remembers it |
| Give every mutating endpoint an `Idempotency-Key` requirement | Assume a caller "probably won't retry" a financial mutation |
| Keep DTOs and domain aggregates as separate types | Reuse a JPA entity as both the persistence model and the API response body |
| Fail closed on a correctness-critical dependency (DB, HSM/KMS) | Fail open / fall back to an insecure default when a dependency is unreachable |
| Write the state change and its Outbox event in one local transaction | Publish an event as a best-effort, post-commit side call |
| Log the fact that an operation happened, at `INFO`, unconditionally | Sample away log lines needed to reconstruct an incident |
| Give a new provider/vendor integration its own adapter-layer package | Add a vendor-specific `if` branch inside a domain or application class |
| Return a generic `403`/`401` for any security-relevant denial | Return a descriptive error revealing why a caller specifically failed authorization |
| Add a package for a genuinely cross-cutting component | Force every cross-cutting concern to live awkwardly inside `application/` just to match a rigid template |

---

# 12. Summary

These guidelines are already in force across every service in this platform — this document simply names them explicitly in one place so a new engineer (or a new AI-assisted code-generation pass) doesn't have to reverse-engineer the pattern from six separate specs. The core discipline is small and repeats everywhere: Clean Architecture layering with dependencies pointing inward only, immutable DTOs distinct from persistence entities, sealed types for every lifecycle state, centralized exception handling mapped to a shared error taxonomy, structural (not just conventional) exclusion of sensitive data from logs, and test coverage that treats an untested state-machine branch as a correctness defect rather than a nitpick. Where a service deviates from the default package template — API Gateway's missing `controller`/`repository`, Token Vault's dual-listener `controller` split, Payment Orchestrator's standalone `saga`/`routing` packages — the deviation is always documented and justified, never silent.