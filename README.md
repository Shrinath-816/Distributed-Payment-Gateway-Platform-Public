# Phase 00 Implementation Specification

Document status: Single source of truth for Phase 0 implementation. This document contains specifications only — no code, no XML, no YAML, no SQL. It is written in strict dependency order: a file is never specified before the files it depends on.

## Documentation & Learning Standard (applies to every file below)

Every generated Java file must begin with a Javadoc block (below `package`, above the type declaration) covering: file location, module, service, package, layer, purpose, why it exists, why it belongs in this layer/package, where it fits in the overall architecture, upstream/downstream dependencies, responsibilities, a 2–5 bullet internal workflow, business rules (if any), security considerations (if any), future extensibility notes, and a cross-reference to the relevant architecture-document section (given per file below as "Doc Cross-Reference"). Every public class/method needs its own Javadoc explaining *why*, not just *what*. Comments in the method body are reserved for non-obvious business logic, concurrency, security, or distributed-systems reasoning — never restating the obvious.

---

# Section 1: Parent Maven Project

### pom.xml
**Relative Path:** `pom.xml`

**1. File Overview**
- Module: root reactor
- Service: N/A (build orchestration only)
- Layer: Build configuration
- Doc Cross-Reference: `SYSTEM_DESIGN.md` §13 (Repository Structure), `02_ENGINEERING_STANDARDS.md` (Java 21 / Spring Boot 3.x standards)

**2. Purpose**
- Declares the Maven reactor (`packaging: pom`) listing every module in build order: `platform/common-core`, `platform/common-security`, `platform/common-kafka`, `platform/common-observability`, `platform/common-test-support`, followed by each of the seven services and `provider-simulator`.
- Centralizes dependency and plugin version management so no child module pins its own version independently.
- Establishes Java 21 as the compiler source/target platform-wide.

**3. Responsibilities**
- Owns `<dependencyManagement>` for Spring Boot BOM, Spring Cloud/Resilience4j BOM (if used), Testcontainers BOM, and internal `platform/*` artifact versions.
- Owns `<pluginManagement>` for the Maven Compiler Plugin (Java 21), Surefire/Failsafe (unit vs. `src/it` integration test separation), and the Spring Boot Maven Plugin.
- Must NOT declare business dependencies (Kafka client, Postgres driver, etc.) directly as `<dependencies>` — only within `<dependencyManagement>`, so each module opts in explicitly.
- Must NOT contain any service-specific configuration.

**4. Dependencies**
- Internal: none (this is the root).
- External: Spring Boot parent BOM, JUnit 5 BOM, Testcontainers BOM.

**5. Public API**
- N/A (build file, no Java API).

**6. Internal Workflow**
- Maven reads `<modules>` and builds each in declared order, respecting inter-module dependency edges resolved from each module's own `pom.xml`.

**7. Engineering Considerations**
- Reproducible builds: every version pinned once, here, never floating (`LATEST`/`RELEASE` forbidden).
- `maven-enforcer-plugin` should be configured here to fail the build on version convergence conflicts.

**8. Testing Strategy**
- No unit tests apply to a POM file; verified transitively by `mvn clean install` succeeding across all modules (see Section 7 of this document).

**9. Future Extension**
- New services are added as a new `<module>` entry only — no other change to this file should be required for a new service to join the reactor.

---

# Section 2: platform/common-core

Dependency order within this module: `ErrorCode` → `BaseException` → `GlobalErrorAttributes` → `ErrorResponse` → `Result` → `HeaderNames` → `ScopeConstants` → `CorrelationIdGenerator` → `IdempotencyKeyValidator` → `IdempotencyRecordCleanupJob`.

### ErrorCode.java
**Relative Path:** `platform/common-core/src/main/java/com/paymentgateway/common/exception/ErrorCode.java`

**1. File Overview**
- Module: common-core | Service: shared | Package: `common.exception` | Layer: Domain-adjacent shared kernel
- Doc Cross-Reference: `API-Standards.md` §12 (Error Codes Table)

**2. Purpose**
- Provides the platform-wide, closed vocabulary of machine-readable error codes referenced in every service's error envelope, per the single shared error contract defined in `API-Standards.md`.
- Exists in common-core because every service's exception handling must agree on the same code strings — duplicating this enum per service would immediately cause drift.

**3. Responsibilities**
- Defines an enum (Java 21 enum, not a sealed interface, since no per-code behavior varies) with the platform-wide baseline codes: `UNAUTHENTICATED`, `FORBIDDEN_ROUTE_CLASS`, `MISSING_IDEMPOTENCY_KEY`, `RATE_LIMIT_EXCEEDED`, `DOWNSTREAM_UNAVAILABLE`, `DOWNSTREAM_TIMEOUT`, `VALIDATION_FAILED`, `RESOURCE_NOT_FOUND`, `RESOURCE_CONFLICT`, `INTERNAL_ERROR`.
- Must NOT contain service-specific codes (e.g. `TOKEN_NOT_ACTIVE`, `MERCHANT_NOT_ACTIVE`) — those are declared inside each service's own merged exception file (e.g. `VaultExceptions.java`) as a service-local enum or as a code string carried on the exception, referencing this shared enum only for the baseline categories.
- Must expose the associated default HTTP status per code, since every service's `GlobalExceptionHandler` maps 1:1 from `ErrorCode` to status.

**4. Dependencies**
- Internal: none (leaf class).
- External: none (pure Java 21 enum).

**5. Public API**
- `ErrorCode` enum constants as described above.
- `int defaultHttpStatus()` — returns the conventional HTTP status code associated with this error code (e.g. `RATE_LIMIT_EXCEEDED` → 429). No parameters. Never throws.

**6. Internal Workflow**
- Purely declarative; no runtime workflow beyond constant lookup.

**7. Engineering Considerations**
- Adding a new baseline code here is a platform-wide, cross-service decision — must be reflected in `API-Standards.md` §12 first (docs are the source of truth; this enum follows the doc, never the other way around).

**8. Testing Strategy**
- Unit test asserting every enum constant has a non-null `defaultHttpStatus()`.

**9. Future Extension**
- New baseline codes are additive (new enum constants); existing constants are never renamed or removed once shipped, since client-facing error bodies depend on the string value.

---

### BaseException.java
**Relative Path:** `platform/common-core/src/main/java/com/paymentgateway/common/exception/BaseException.java`

**1. File Overview**
- Module: common-core | Package: `common.exception` | Layer: Domain-adjacent shared kernel
- Doc Cross-Reference: `Coding-Guidelines.md` §6 (Exception Handling)

**2. Purpose**
- The single root exception type every service's domain-specific exceptions extend, ensuring every thrown business exception carries a structured `ErrorCode` rather than relying on `getMessage()` parsing.

**3. Responsibilities**
- Carries an `ErrorCode`, a human-readable-but-safe `message`, and an optional `details` list (field-level validation issues) per the platform error envelope shape.
- Must NOT carry a raw `Throwable` cause chain into the external response — internal cause is for logging only, never serialized.
- Must NOT be thrown directly (abstract class) — every service extends it with a specific named exception (e.g. `MerchantExceptions.MerchantNotFoundException`).

**4. Dependencies**
- Internal: `ErrorCode`.
- External: extends `RuntimeException` (unchecked, per platform convention — business exceptions are not recoverable inline, they propagate to the exception handler).

**5. Public API**
- Constructor `BaseException(ErrorCode errorCode, String message)` — sets code/message, no cause.
- Constructor `BaseException(ErrorCode errorCode, String message, List<String> details)` — same, with structural validation detail list.
- `ErrorCode getErrorCode()` — accessor, no side effects.
- `List<String> getDetails()` — accessor, returns empty list (never null) if none supplied.

**6. Internal Workflow**
- Construction only stores fields; all mapping-to-HTTP-response behavior lives in each service's `GlobalExceptionHandler`, not here.

**7. Engineering Considerations**
- Security: message text must never be allowed to carry an internal identifier, stack frame, or cardholder-data-adjacent value — enforced by code review convention, not by this class itself.
- Immutability: fields are `final`, set only via constructor.

**8. Testing Strategy**
- Unit test verifying `getDetails()` returns an empty (not null) list when omitted.

**9. Future Extension**
- If a future need arises for exception-level retry hints, add a boolean/enum field here rather than creating a parallel exception hierarchy.

---

### GlobalErrorAttributes.java
**Relative Path:** `platform/common-core/src/main/java/com/paymentgateway/common/exception/GlobalErrorAttributes.java`

**1. File Overview**
- Module: common-core | Package: `common.exception` | Layer: Infrastructure (Spring Boot error-attribute customization)
- Doc Cross-Reference: `API-Standards.md` §7 (Error Format)

**2. Purpose**
- Supplies a shared, reusable `ErrorAttributes` (Spring Boot / WebFlux `DefaultErrorAttributes` override) that renders any uncaught exception into the platform's exact error envelope shape, as a last-resort safety net beneath each service's own `GlobalExceptionHandler`.

**3. Responsibilities**
- Converts a generic unhandled `Throwable` into the standard envelope with `ErrorCode.INTERNAL_ERROR`, current `correlationId` (read from request context, not regenerated), and current UTC timestamp.
- Must NOT be the primary error-handling path for expected/business exceptions — those are handled by each service's own `@ControllerAdvice`/`GlobalExceptionHandler`; this class only guarantees the envelope shape is never broken for a truly unexpected failure.
- Must NOT leak the original exception's message or stack trace into the response body.

**4. Dependencies**
- Internal: `ErrorResponse`, `ErrorCode`, `HeaderNames` (to read the correlation ID attribute).
- External: Spring Boot `ErrorAttributes` / `DefaultErrorAttributes`.

**5. Public API**
- Method overriding `getErrorAttributes(ServerRequest/WebRequest, ErrorAttributeOptions)` — Purpose: build the response map; Parameters: the inbound request and Spring's attribute options; Return: a `Map<String,Object>` matching `ErrorResponse`'s field shape; Exceptions: none (must never itself throw).

**6. Internal Workflow**
- Read the correlation ID already attached to the request (set upstream by `CorrelationIdFilter` in common-observability).
- Construct an `ErrorResponse` with `ErrorCode.INTERNAL_ERROR`, a fixed generic message, empty details, current timestamp, and the correlation ID.
- Return it as a map (framework requirement) rather than the typed object directly.

**7. Engineering Considerations**
- Logging: the real exception and stack trace must still be logged at `ERROR` server-side before this class strips it from the response — logging responsibility can live here or in a wrapping filter, but must happen exactly once (never duplicated).
- Security: this is the platform's outermost safety net against accidental information disclosure on an unanticipated failure.

**8. Testing Strategy**
- Integration test throwing an unexpected `RuntimeException` from a test controller and asserting the response body matches the standard envelope with no message/stack leakage.

**9. Future Extension**
- If per-environment verbosity is ever needed (e.g. showing more detail in `dev`), gate it behind an explicit profile check here — never by default.

---

### ErrorResponse.java
**Relative Path:** `platform/common-core/src/main/java/com/paymentgateway/common/dto/ErrorResponse.java`

**1. File Overview**
- Module: common-core | Package: `common.dto` | Layer: Shared DTO
- Doc Cross-Reference: `API-Standards.md` §7 (Error Format)

**2. Purpose**
- The single, platform-wide error-envelope DTO (Java 21 `record`) every service returns for every error response, replacing what would otherwise be seven near-identical per-service `ErrorResponse` classes (an over-abstraction the re-engineering pass explicitly removed).

**3. Responsibilities**
- Fields: `code` (String, from `ErrorCode.name()`), `message` (String), `correlationId` (String), `timestamp` (Instant), `details` (`List<String>`, defaults to empty).
- Must NOT contain any field not already present in the documented envelope shape — no service may extend or wrap this record with additional fields; service-specific error detail belongs in `details`, not new top-level fields.

**4. Dependencies**
- Internal: none (used by, not dependent on, `ErrorCode`/`BaseException`/`GlobalErrorAttributes`).
- External: `java.time.Instant`, `java.util.List`.

**5. Public API**
- Canonical record constructor `ErrorResponse(String code, String message, String correlationId, Instant timestamp, List<String> details)`.
- Static factory `ErrorResponse.of(ErrorCode code, String message, String correlationId)` — Purpose: convenience construction with empty details and `Instant.now()`; Parameters: as named; Return: a new `ErrorResponse`; Exceptions: none.

**6. Internal Workflow**
- Pure data carrier; no workflow.

**7. Engineering Considerations**
- Serialization: must serialize to exactly the JSON shape documented in `API-Standards.md` §7 (nested under an `"error"` key at the point of serialization — whether that nesting is applied here or by the calling `GlobalExceptionHandler` must be decided once and applied identically by every service; recommended: this record represents the inner `error` object only, and each service's handler wraps it under `{"error": ...}` at the point of response construction).

**8. Testing Strategy**
- Serialization test asserting field names/casing match the documented contract exactly.

**9. Future Extension**
- Any new field must be optional/nullable and additive, never breaking existing consumers' deserialization.

---

### Result.java
**Relative Path:** `platform/common-core/src/main/java/com/paymentgateway/common/result/Result.java`

**1. File Overview**
- Module: common-core | Package: `common.result` | Layer: Shared kernel (application-layer helper)
- Doc Cross-Reference: `Coding-Guidelines.md` §3 (Layer Responsibilities), §10 (Clean Architecture Rules)

**2. Purpose**
- A generic success/failure wrapper (Java 21 sealed interface with two record implementations, `Success<T>` and `Failure`) available to any service's application-layer use case that prefers an explicit result type over throwing for expected, non-exceptional business outcomes (e.g. a validation-style rejection that isn't severe enough to warrant an exception-handler round trip).
- Optional, not mandatory — most use cases in this platform throw `BaseException` subtypes per `Coding-Guidelines.md` §6, and `Result` exists only for the narrow cases (documented per-service) where a use case's caller needs to branch on outcome without exception-handling overhead.

**3. Responsibilities**
- `Success<T>` wraps a value; `Failure` wraps an `ErrorCode` and message.
- Must NOT be used as a substitute for the platform's exception-based error handling for anything crossing a controller boundary — `Result` is an internal, application-layer-only construct; controllers always translate to either a normal response or let a `BaseException` propagate to the handler.

**4. Dependencies**
- Internal: `ErrorCode`.
- External: none.

**5. Public API**
- `static <T> Result<T> success(T value)` — Purpose: wrap a successful value; Return: `Success<T>`.
- `static <T> Result<T> failure(ErrorCode code, String message)` — Purpose: wrap a failure; Return: `Failure`.
- `boolean isSuccess()` / `boolean isFailure()` — accessors.
- `T getOrThrow()` — Purpose: unwrap a `Success`, or throw a `BaseException`-derived exception carrying the wrapped `ErrorCode`/message if this is a `Failure`; Exceptions: throws on `Failure`.

**6. Internal Workflow**
- Pattern-matched via Java 21 `switch` on the sealed type at the call site; no internal state machine.

**7. Engineering Considerations**
- Thread safety: immutable records, inherently thread-safe.

**8. Testing Strategy**
- Unit tests for `getOrThrow()` on both branches.

**9. Future Extension**
- Could gain a `map`/`flatMap` combinator later if functional-style chaining becomes a recurring need — not implemented now per the Simplicity First principle (no speculative API surface).

---

### HeaderNames.java
**Relative Path:** `platform/common-core/src/main/java/com/paymentgateway/common/constant/HeaderNames.java`

**1. File Overview**
- Module: common-core | Package: `common.constant` | Layer: Shared kernel
- Doc Cross-Reference: `API-Standards.md` §4 (Headers)

**2. Purpose**
- Single source of truth for every platform-standard HTTP header name string, preventing typo-prone duplicated literals (`"X-Correlation-Id"` etc.) across seven services.

**3. Responsibilities**
- Defines `public static final String` constants: `AUTHORIZATION`, `X_API_KEY`, `IDEMPOTENCY_KEY`, `X_CORRELATION_ID`, `TRACEPARENT`, `TRACESTATE`, `X_MERCHANT_ID`, `CONTENT_TYPE`, `RETRY_AFTER`.
- Must NOT contain any header value or parsing logic — names only.

**4. Dependencies**
- Internal/External: none.

**5. Public API**
- Public static final String fields only; no methods (final utility class, private constructor to prevent instantiation).

**6. Internal Workflow**
- N/A.

**7. Engineering Considerations**
- N/A beyond immutability.

**8. Testing Strategy**
- No behavior to test; compilation is the verification.

**9. Future Extension**
- New platform-standard headers are added here first, before any service references the literal string.

---

### ScopeConstants.java
**Relative Path:** `platform/common-core/src/main/java/com/paymentgateway/common/constant/ScopeConstants.java`

**1. File Overview**
- Module: common-core | Package: `common.constant` | Layer: Shared kernel
- Doc Cross-Reference: `Security-Architecture.md` §3 (Authorization)

**2. Purpose**
- Single source of truth for the platform's authorization scope-string vocabulary (`payments:read`, `payments:write`, `merchant:admin`, etc.), shared between the API Gateway (coarse route-class checks) and Merchant Service (credential scope issuance/validation).

**3. Responsibilities**
- Defines `public static final String` scope constants and, optionally, a `Set<String> ALL_SCOPES` for validation against unrecognized scope strings.
- Must NOT encode which route requires which scope — that mapping is the API Gateway's own `RouteDefinition`/`ScopePolicy` configuration, not this class's concern.

**4. Dependencies**
- Internal/External: none.

**5. Public API**
- Public static final String fields; `public static final Set<String> ALL_SCOPES`.

**6. Internal Workflow**
- N/A.

**7. Engineering Considerations**
- N/A.

**8. Testing Strategy**
- Unit test asserting `ALL_SCOPES` contains every declared constant (prevents a forgotten registration).

**9. Future Extension**
- New scopes added here; Merchant Service's `ScopeValidator` (Phase 1) references `ALL_SCOPES` to reject unrecognized values.

---

### CorrelationIdGenerator.java
**Relative Path:** `platform/common-core/src/main/java/com/paymentgateway/common/util/CorrelationIdGenerator.java`

**1. File Overview**
- Module: common-core | Package: `common.util` | Layer: Shared kernel utility
- Doc Cross-Reference: `Observability.md` §7 (Log Fields Table, `correlationId`)

**2. Purpose**
- Generates a new correlation ID when an inbound request doesn't already carry one, used by `CorrelationIdFilter` (common-observability) across every service.

**3. Responsibilities**
- Generates a UUIDv4 string suitable for cross-service correlation.
- Must NOT attempt to validate or parse an *existing* incoming correlation ID's format — an upstream-supplied value is passed through unchanged regardless of its shape, per platform convention (never rejecting a caller's correlation ID).

**4. Dependencies**
- Internal/External: `java.util.UUID`.

**5. Public API**
- `static String generate()` — Purpose: produce a new correlation ID; Parameters: none; Return: UUIDv4 string; Exceptions: none.

**6. Internal Workflow**
- Delegates directly to `UUID.randomUUID().toString()`.

**7. Engineering Considerations**
- Uses the JVM's default (non-cryptographic) UUID generator — sufficient here since a correlation ID is a tracing convenience, not a security token (unlike `VaultTokenId` in Token Vault, which has a materially different, security-relevant generation requirement specified in its own service).

**8. Testing Strategy**
- Unit test asserting uniqueness across repeated calls and valid UUID format.

**9. Future Extension**
- None anticipated; this is intentionally the simplest possible implementation.

---

### IdempotencyKeyValidator.java
**Relative Path:** `platform/common-core/src/main/java/com/paymentgateway/common/util/IdempotencyKeyValidator.java`

**1. File Overview**
- Module: common-core | Package: `common.util` | Layer: Shared kernel utility
- Doc Cross-Reference: `API-Standards.md` §10 (Idempotency)

**2. Purpose**
- Shared structural validation for the `Idempotency-Key` header value (must be UUID-format), used by every service's mutating-endpoint request validation, per the platform-wide idempotency standard.

**3. Responsibilities**
- Validates that a supplied string is a well-formed UUID.
- Must NOT perform the actual deduplication lookup (Redis/DB check) — that is each service's own `IdempotencyRecord` repository concern (Phase 1+); this class only validates the header's *format*.

**4. Dependencies**
- Internal/External: `java.util.UUID`.

**5. Public API**
- `static boolean isValid(String idempotencyKey)` — Purpose: format check; Parameters: the raw header string (may be null); Return: `false` for null/blank/non-UUID, `true` otherwise; Exceptions: none (never throws — a caller decides how to react to `false`).

**6. Internal Workflow**
- Null/blank short-circuit → `false`.
- Attempt `UUID.fromString(value)`; catch `IllegalArgumentException` → `false`; success → `true`.

**7. Engineering Considerations**
- Deliberately side-effect-free and exception-free so every calling filter/validator can use it in a simple boolean branch without a try/catch of its own.

**8. Testing Strategy**
- Unit tests: valid UUID, null, blank, malformed string, UUID with wrong dash placement.

**9. Future Extension**
- None anticipated.

---

### IdempotencyRecordCleanupJob.java
**Relative Path:** `platform/common-core/src/main/java/com/paymentgateway/common/scheduling/IdempotencyRecordCleanupJob.java`

**1. File Overview**
- Module: common-core | Package: `common.scheduling` | Layer: Shared kernel (scheduled infrastructure)
- Doc Cross-Reference: `Coding-Guidelines.md` §1 (Project Structure — cross-cutting component consolidation rationale)

**2. Purpose**
- A single, reusable scheduled job that purges expired `idempotency_record` rows, replacing what would otherwise be three near-identical per-service scheduler classes (Payment Orchestrator, Acquiring Adapter, and any future service with an idempotency table) doing the exact same "delete where `created_at` older than TTL" operation.
- Lives in common-core (not common-kafka) because it is a pure scheduling + cleanup concern with no messaging involvement.

**3. Responsibilities**
- Defines a small port interface, `IdempotencyRecordPurgePort` (declared in this same file or an adjacent file in this package), with one method each implementing service's own JPA adapter implements, so common-core never depends on JPA/Hibernate directly.
- On a configurable fixed-delay schedule, invokes purge on every `IdempotencyRecordPurgePort` bean registered in the Spring context (each service registers exactly one, wired to its own repository).
- Must NOT know about any service's specific `IdempotencyRecordEntity` shape — it only calls the port's `purgeExpiredRecords(Duration ttl)` method and logs the returned count.

**4. Dependencies**
- Internal: none beyond its own port interface.
- External: Spring's `@Scheduled` (or a `TaskScheduler` bean supplied by each service), Micrometer (to emit a purge-count metric).

**5. Public API**
- `IdempotencyRecordPurgePort` interface method: `int purgeExpiredRecords(Duration ttl)` — Purpose: delete/expire idempotency rows older than `ttl`; Parameters: TTL duration; Return: number of rows purged; Exceptions: implementation-specific (data-access exceptions propagate; the job catches and logs rather than failing the whole scheduled run if one port implementation throws, so one service's failure doesn't block another's cleanup in the same JVM — relevant only if multiple services somehow shared a JVM, which they don't in this platform's deployment model, but the defensive per-port try/catch is still the correct design).
- `IdempotencyRecordCleanupJob` constructor accepting `List<IdempotencyRecordPurgePort>` and a configured TTL `Duration`.
- Scheduled method `runCleanup()` — Purpose: the `@Scheduled` entry point; Parameters: none; Return: void; Exceptions: none escape (internally caught and logged per port).

**6. Internal Workflow**
- On each scheduled tick, iterate every registered `IdempotencyRecordPurgePort` bean.
- Call `purgeExpiredRecords(ttl)`, catching and logging any exception per-port so one failing service-adapter doesn't prevent others (relevant if this job is ever reused across a multi-tenant test harness; in production each service runs its own JVM with exactly one port bean).
- Emit a Micrometer counter/gauge of rows purged for observability.

**7. Engineering Considerations**
- Concurrency: relies on the underlying database's own row-level operations for correctness; this job itself does not need distributed locking, since a duplicate concurrent purge run is idempotent by nature (deleting already-deleted rows is a no-op).
- Configuration: fixed-delay interval and TTL duration are externalized properties, not hardcoded, so each service can tune them via its own `application.yml` without touching this shared class.

**8. Testing Strategy**
- Unit test with a fake `IdempotencyRecordPurgePort` verifying the job calls purge and handles a thrown exception from one port without preventing invocation of others.
- Each service's own integration test (Phase 1+) verifies its concrete adapter correctly deletes only expired rows.

**9. Future Extension**
- If a future service needs a different cleanup cadence than the shared default, expose the schedule as a per-service property override rather than forking this class.

---

# Section 3: platform/common-security

Dependency order: `WorkloadIdentity` → `MtlsIdentityExtractor` → `MtlsAllowListProperties` → `JwksKeyCache` → `JwtValidator` → `SecurityBaseConfig`.

### WorkloadIdentity.java
**Relative Path:** `platform/common-security/src/main/java/com/paymentgateway/common/mtls/WorkloadIdentity.java`

**1. File Overview**
- Module: common-security | Package: `common.mtls` | Layer: Shared kernel
- Doc Cross-Reference: `Security-Architecture.md` §6 (mTLS Flow)

**2. Purpose**
- A small immutable value type (Java 21 `record`) representing an internal caller's verified workload identity (e.g. `spiffe://platform/payment-orchestrator`), the platform-wide unit every internal allow-list check compares against.

**3. Responsibilities**
- Fields: `identity` (String, the SPIFFE-like URI or certificate CN), `serviceName` (String, a derived short name for logging).
- Must NOT contain any certificate byte data itself — this is a resolved, already-verified identity, not raw certificate material.

**4. Dependencies**
- Internal/External: none.

**5. Public API**
- Canonical record constructor.
- `static WorkloadIdentity of(String identity)` — Purpose: convenience factory deriving `serviceName` from the identity string's last path segment; Return: new instance.

**6. Internal Workflow**
- N/A (pure data carrier).

**7. Engineering Considerations**
- Immutability guarantees this can be safely placed in a Spring Security context/principal without defensive copying.

**8. Testing Strategy**
- Unit test for `of()`'s `serviceName` derivation on a representative identity string.

**9. Future Extension**
- If richer identity metadata is needed (e.g. certificate expiry for logging), add fields here rather than creating a parallel type.

---

### MtlsIdentityExtractor.java
**Relative Path:** `platform/common-security/src/main/java/com/paymentgateway/common/mtls/MtlsIdentityExtractor.java`

**1. File Overview**
- Module: common-security | Package: `common.mtls` | Layer: Infrastructure (security filter helper)
- Doc Cross-Reference: `Security-Architecture.md` §6 (mTLS Flow), `Token-Vault-Part-02.md` §19.8 (Certificate Validation)

**2. Purpose**
- Extracts a `WorkloadIdentity` from the verified peer certificate on an mTLS-terminated internal request, shared by every service's own internal-surface allow-list filter rather than each service reimplementing certificate-attribute parsing.

**3. Responsibilities**
- Reads the peer certificate's Subject (or SAN URI, depending on the mesh's certificate profile) from the underlying request/connection object and produces a `WorkloadIdentity`.
- Must NOT perform certificate-chain validation itself — that is the service mesh's/TLS termination layer's job (per `Security-Architecture.md` §6); this class only reads an already-trusted, already-validated certificate's identity field.
- Must return an explicit "absent" result (e.g. `Optional<WorkloadIdentity>`) rather than throwing, when no client certificate is present, so callers decide how to react (reject vs. treat as unauthenticated public traffic, relevant for Token Vault's dual-listener design).

**4. Dependencies**
- Internal: `WorkloadIdentity`.
- External: Servlet/WebFlux request abstraction exposing the peer certificate (framework-specific; exact type resolved during implementation based on whether a service is Servlet-stack or WebFlux-stack per its own spec).

**5. Public API**
- `Optional<WorkloadIdentity> extract(<RequestType> request)` — Purpose: pull identity from the verified peer cert; Parameters: the inbound request/exchange object; Return: `Optional.empty()` if no client certificate present; Exceptions: none (malformed certificate data is treated as absent, not thrown).

**6. Internal Workflow**
- Retrieve the peer certificate attribute from the request/connection.
- If absent, return `Optional.empty()`.
- If present, extract the identity string per the mesh's certificate profile and wrap it via `WorkloadIdentity.of(...)`.

**7. Engineering Considerations**
- Security: this class must never fabricate or default an identity when none is present — an absent identity must always be treated as "unauthenticated," never silently substituted.

**8. Testing Strategy**
- Unit tests with a mocked request: certificate present with valid identity, certificate present with malformed identity string, certificate entirely absent.

**9. Future Extension**
- If the platform adopts a different mesh with a different certificate-identity convention, only this class's extraction logic changes — every consuming filter stays the same.

---

### MtlsAllowListProperties.java
**Relative Path:** `platform/common-security/src/main/java/com/paymentgateway/common/config/MtlsAllowListProperties.java`

**1. File Overview**
- Module: common-security | Package: `common.config` | Layer: Infrastructure (externalized configuration)
- Doc Cross-Reference: `Security-Architecture.md` §3 (Authorization, Service Roles table)

**2. Purpose**
- A `@ConfigurationProperties`-bound class each service uses to externalize its own internal-surface allow-list (which `WorkloadIdentity` values may call which internal role), rather than hardcoding identity strings in Java.

**3. Responsibilities**
- Binds a map/list structure from `application.yml` (e.g. `security.mtls.allow-list.<role-name>: [ identity1, identity2 ]`).
- Must NOT contain any allow-list *values* itself — this class is the binding shape only; the actual allow-listed identities are each service's own environment-specific configuration data.

**4. Dependencies**
- Internal/External: Spring Boot `@ConfigurationProperties`.

**5. Public API**
- Getter/setter (or constructor-binding, per Spring Boot 3.x immutable `@ConfigurationProperties` convention) for the bound map/list structure.
- `boolean isAllowed(String role, WorkloadIdentity identity)` — Purpose: convenience check used by a service's allow-list filter; Parameters: the required role name and the caller's resolved identity; Return: boolean; Exceptions: none.

**6. Internal Workflow**
- Spring Boot binds the YAML structure at startup; `isAllowed` performs a simple set-membership check against the bound data.

**7. Engineering Considerations**
- Fail-closed: if a role is not present in the bound configuration at all, `isAllowed` must return `false`, never `true` by default — an unconfigured role has no allowed callers, not unlimited ones.

**8. Testing Strategy**
- Unit test with a sample bound map verifying allowed/disallowed identities per role, and the fail-closed behavior for an unconfigured role.

**9. Future Extension**
- If per-role rate limits or additional metadata are needed alongside the identity list, extend the bound shape here.

---

### JwksKeyCache.java
**Relative Path:** `platform/common-security/src/main/java/com/paymentgateway/common/jwt/JwksKeyCache.java`

**1. File Overview**
- Module: common-security | Package: `common.jwt` | Layer: Infrastructure
- Doc Cross-Reference: `API-Gateway-Part-02.md` §25.2 (JWT Validation detail)

**2. Purpose**
- Caches the Identity Provider's JWKS public key set with a short TTL, avoiding a remote key-fetch on every single JWT validation, used primarily by the API Gateway's `JwtValidator` but placed in common-security in case any other service ever needs local JWT validation.

**3. Responsibilities**
- Fetches and caches the current JWKS key set keyed by `kid` (key ID).
- Refreshes on cache-miss for an unrecognized `kid` (supports key rotation without redeploy) and otherwise on a fixed TTL (e.g. 15 minutes, externalized as a property).
- Must NOT validate a JWT itself — that is `JwtValidator`'s job; this class only resolves the correct public key for a given `kid`.

**4. Dependencies**
- Internal: none.
- External: an HTTP client to fetch the JWKS endpoint, a JSON/JWK parsing library (e.g. Nimbus JOSE+JWT, consistent with whatever JWT library `JwtValidator` uses).

**5. Public API**
- `PublicKey getKey(String kid)` — Purpose: resolve the public key for a given key ID, fetching/refreshing as needed; Parameters: the JWT header's `kid`; Return: the matching `PublicKey`; Exceptions: throws a checked/unchecked `KeyResolutionException` (or similar) if the `kid` cannot be resolved even after a refresh attempt.

**6. Internal Workflow**
- Check in-memory cache for `kid`.
- On hit and not expired, return immediately.
- On miss or expiry, fetch the current JWKS document from the configured issuer URI, rebuild the cache, and retry the lookup once.
- If still unresolved, throw.

**7. Engineering Considerations**
- Concurrency: cache refresh must be safe under concurrent requests (a single in-flight refresh shared by simultaneous callers, not a refresh-per-request stampede) — implement via a lock or a single-flight pattern.
- Resilience: the remote JWKS fetch should be wrapped with the platform's Resilience4j circuit breaker/timeout convention (configured by the consuming service, not hardcoded here).

**8. Testing Strategy**
- Unit tests with a mocked JWKS HTTP source: cache hit, cache miss triggering refresh, refresh failure resulting in the resolution exception, concurrent-miss single-flight behavior.

**9. Future Extension**
- If the platform later supports multiple Identity Provider issuers, key the cache by `(issuer, kid)` rather than `kid` alone.

---

### JwtValidator.java
**Relative Path:** `platform/common-security/src/main/java/com/paymentgateway/common/jwt/JwtValidator.java`

**1. File Overview**
- Module: common-security | Package: `common.jwt` | Layer: Infrastructure
- Doc Cross-Reference: `API-Gateway-Part-02.md` §25.2 (JWT Validation)

**2. Purpose**
- Validates an inbound Bearer JWT's signature, expiry, issuer, and audience per the platform's JWT authentication standard, used by the API Gateway (the platform's sole JWT-validating component per `Security-Architecture.md` §2).

**3. Responsibilities**
- Validates signature using RS256/ES256 only (explicit algorithm allow-list — never trusts the token's own `alg` header without cross-checking against the configured allow-list, preventing algorithm-confusion attacks).
- Validates `exp`, `nbf` (±60s clock skew tolerance), `iss`, `aud` claims against configured expected values.
- Must NOT accept HS256 or any symmetric algorithm from an external caller under any configuration.
- Must NOT perform authorization (scope checking) — this class answers only "is this token authentic and current," not "is this caller allowed to do X."

**4. Dependencies**
- Internal: `JwksKeyCache`.
- External: a JWT parsing library (e.g. Nimbus JOSE+JWT / `java-jwt`), consistent platform-wide once chosen here.

**5. Public API**
- `AuthenticatedClaims validate(String rawJwt)` — Purpose: full validation pipeline; Parameters: the raw Bearer token string (without the `"Bearer "` prefix, stripped by the caller); Return: a small claims value object (subject, scopes, merchant/client identifier) on success; Exceptions: throws a specific unchecked exception (e.g. `InvalidTokenException`) with enough detail for internal logging but never enough to leak into an external response body (per `API-Gateway-Part-02.md` §19.11's generic-denial requirement).

**6. Internal Workflow**
- Parse the JWT header to read `alg` and `kid` without yet trusting them.
- Reject immediately if `alg` is not in the RS256/ES256 allow-list.
- Resolve the public key via `JwksKeyCache.getKey(kid)`.
- Verify signature with that key.
- Validate `exp`/`nbf`/`iss`/`aud` claims.
- On full success, map claims into `AuthenticatedClaims` and return.

**7. Engineering Considerations**
- Security: every rejection path (bad algorithm, bad signature, expired, wrong issuer/audience) must be logged with enough detail for security monitoring while the exception surfaced to the caller stays generic.
- Performance: relies entirely on `JwksKeyCache` to avoid a remote call on the hot authentication path.

**8. Testing Strategy**
- Unit tests: valid token, expired token, wrong issuer, wrong audience, HS256-signed token (must be rejected outright), tampered signature, clock-skew boundary cases (±60s).

**9. Future Extension**
- If OpenID Connect discovery is adopted later, issuer/audience configuration could be resolved dynamically rather than statically configured — a change confined to this class and `JwksKeyCache`.

---

### SecurityBaseConfig.java
**Relative Path:** `platform/common-security/src/main/java/com/paymentgateway/common/config/SecurityBaseConfig.java`

**1. File Overview**
- Module: common-security | Package: `common.config` | Layer: Infrastructure (Spring configuration)
- Doc Cross-Reference: `Security-Architecture.md` §2–§3 (Authentication, Authorization)

**2. Purpose**
- Provides the shared baseline Spring Security filter-chain configuration (stateless session policy, CSRF disabled for a pure API surface, common security headers) every service imports and extends with its own service-specific filters (e.g. `AuthenticationFilter` at the Gateway, `InternalServiceAuthFilter` elsewhere).

**3. Responsibilities**
- Configures `SessionCreationPolicy.STATELESS`, disables CSRF (appropriate for a bearer-token/mTLS API platform with no cookie-based session), and registers common security headers (e.g. HSTS where applicable).
- Must NOT register any service-specific filter itself — each service's own `SecurityConfig` composes this base with its own filters.

**4. Dependencies**
- Internal/External: Spring Security (`SecurityFilterChain` / `ServerHttpSecurity` depending on Servlet vs. WebFlux stack per service).

**5. Public API**
- A `@Bean`-producing method returning the base security configuration object each service's own `SecurityConfig` further customizes (exact return type — `HttpSecurity`/`ServerHttpSecurity` builder or a reusable customizer — resolved at implementation time based on the consuming service's stack).

**6. Internal Workflow**
- Applies the baseline settings described in Responsibilities; delegates everything else to the importing service.

**7. Engineering Considerations**
- Consistency: this is what guarantees no service accidentally reintroduces stateful sessions or leaves CSRF enabled on an API-only surface.

**8. Testing Strategy**
- Each service's own security integration test (Phase 1+) verifies the composed chain behaves as expected; this base class itself only needs a smoke test confirming it produces a non-null, stateless configuration.

**9. Future Extension**
- If a future service genuinely needs cookie-based sessions (unlikely, but e.g. an admin dashboard), that service overrides rather than modifies this shared base.

---

# Section 4: platform/common-kafka

Dependency order: `EventType` → `EventEnvelope` → `OutboxEventStatus` → `OutboxEvent` → `InboxEvent` → `KafkaTopicsProperties` → `KafkaProducerConfig` → `KafkaConsumerConfig` → `OutboxRelay` → `OutboxRelayScheduler` → `InboxDeduplicationService`.

### EventType.java
**Relative Path:** `platform/common-kafka/src/main/java/com/paymentgateway/common/envelope/EventType.java`

**1. File Overview**
- Module: common-kafka | Package: `common.envelope` | Layer: Shared kernel
- Doc Cross-Reference: `Event-Catalog.md` §6 (Event Catalog table), §2 (Event Naming Convention)

**2. Purpose**
- Enumerates every domain event name in the platform's Event Catalog as a closed, typo-proof vocabulary shared by every producer and consumer, rather than each service passing raw strings.

**3. Responsibilities**
- One enum constant per event listed in `Event-Catalog.md` §6 (e.g. `MERCHANT_ACTIVATED`, `TOKEN_CREATED`, `PAYMENT_CAPTURED`, `SETTLEMENT_COMPLETED`, etc.) — the full list is authoritative in that document and must be reproduced exactly, not re-derived or abbreviated.
- Must NOT encode which topic an event belongs to — that mapping lives in `KafkaTopicsProperties`, keeping event identity and topic routing independently configurable.

**4. Dependencies**
- Internal/External: none.

**5. Public API**
- Enum constants only, plus `String wireName()` — Purpose: the exact past-tense string used on the wire/in the `EventEnvelope.eventType` field (may equal `name()` or a explicitly mapped string if casing conventions differ); Return: String; Exceptions: none.

**6. Internal Workflow**
- N/A (declarative).

**7. Engineering Considerations**
- Any new event introduced in a later phase must be added here and cross-checked against `Event-Catalog.md` before that service's producer code is written — this file is intentionally kept in lock-step with the documentation.

**8. Testing Strategy**
- A unit test cross-referencing this enum's constant count/names against a checked-in copy of the Event Catalog list (a simple safeguard against drift, not a live doc-parser).

**9. Future Extension**
- Additive only; an event name is never renamed or removed once any service has shipped a producer/consumer for it (per `Event-Catalog.md` §10, Event Versioning).

---

### EventEnvelope.java
**Relative Path:** `platform/common-kafka/src/main/java/com/paymentgateway/common/envelope/EventEnvelope.java`

**1. File Overview**
- Module: common-kafka | Package: `common.envelope` | Layer: Shared kernel
- Doc Cross-Reference: `SYSTEM_DESIGN.md` §5 (Event Envelope), `Event-Catalog.md` §4 (Event Lifecycle Diagram)

**2. Purpose**
- The single platform-wide event wrapper (Java 21 generic `record`) every service's Outbox row is serialized into and every Kafka message deserializes from, guaranteeing every event on every topic carries the same structural metadata.

**3. Responsibilities**
- Fields, exactly per `SYSTEM_DESIGN.md` §5: `eventId` (UUID), `eventType` (`EventType`), `aggregateId` (UUID), `version` (long), `correlationId` (UUID), `causationId` (UUID, nullable), `timestamp` (Instant, UTC), `payload` (generic type `T`, the event-specific data).
- Must NOT allow `payload` to be an untyped `Object`/`Map` in application code — each producer constructs `EventEnvelope<SpecificPayloadType>`, so payload shape is compile-time checked up to the serialization boundary.
- Must NOT ever contain a field capable of holding cardholder data, secrets, or key material — this is a structural, platform-wide guarantee independent of any single service's own discipline.

**4. Dependencies**
- Internal: `EventType`.
- External: `java.time.Instant`, `java.util.UUID`, a JSON serialization library (Jackson, standard with Spring Boot) for the payload.

**5. Public API**
- Canonical generic record constructor `EventEnvelope<T>(UUID eventId, EventType eventType, UUID aggregateId, long version, UUID correlationId, UUID causationId, Instant timestamp, T payload)`.
- Static factory `static <T> EventEnvelope<T> newEvent(EventType type, UUID aggregateId, long version, UUID correlationId, UUID causationId, T payload)` — Purpose: convenience construction generating a fresh `eventId` and current `timestamp`; Return: new envelope instance.

**6. Internal Workflow**
- Pure data carrier; serialization/deserialization is handled by Jackson configuration in `KafkaProducerConfig`/`KafkaConsumerConfig`, not by this class itself.

**7. Engineering Considerations**
- Generic type erasure: Jackson's generic deserialization requires either a `TypeReference` at the consumer side or a wrapping mechanism (e.g. carrying the payload's fully-qualified class name, or each consumer knowing its expected payload type upfront since it only ever subscribes to specific event types) — this detail must be resolved consistently in `KafkaConsumerConfig` and documented there.

**8. Testing Strategy**
- Serialization round-trip test: construct an envelope with a sample payload type, serialize, deserialize, assert equality.

**9. Future Extension**
- `version` supports future schema evolution per `Event-Catalog.md` §10; new optional payload fields are additive within `T`, never a structural change to the envelope itself.

---

### OutboxEventStatus.java
**Relative Path:** `platform/common-kafka/src/main/java/com/paymentgateway/common/outbox/OutboxEventStatus.java`

**1. File Overview**
- Module: common-kafka | Package: `common.outbox` | Layer: Shared kernel
- Doc Cross-Reference: `SYSTEM_DESIGN.md` §7 (Outbox Pattern)

**2. Purpose**
- The finite status vocabulary for an outbox row's publication lifecycle, shared across every service's outbox table.

**3. Responsibilities**
- Enum constants: `PENDING` (not yet published), `PUBLISHED` (successfully sent to Kafka), `FAILED` (publish attempted and failed beyond retry, held for operator visibility — an addition beyond a simple boolean `published` flag, giving the Outbox Relay a place to park a row that failed repeatedly rather than retrying it forever silently).

**4. Dependencies**
- Internal/External: none.

**5. Public API**
- Enum constants only.

**6. Internal Workflow**
- N/A.

**7. Engineering Considerations**
- Keeping this as a small enum (rather than a boolean) is what allows `OutboxRelay` to distinguish "not yet attempted" from "attempted and permanently failed" without a separate table.

**8. Testing Strategy**
- N/A beyond compilation; behavior tested via `OutboxRelay`'s own tests.

**9. Future Extension**
- If retry-count-aware backoff is added later, keep the status enum as-is and add a separate `attemptCount` field on `OutboxEvent` rather than multiplying status values.

---

### OutboxEvent.java
**Relative Path:** `platform/common-kafka/src/main/java/com/paymentgateway/common/outbox/OutboxEvent.java`

**1. File Overview**
- Module: common-kafka | Package: `common.outbox` | Layer: Shared kernel (framework-agnostic domain-adjacent model)
- Doc Cross-Reference: `SYSTEM_DESIGN.md` §7 (Outbox Pattern)

**2. Purpose**
- A framework-agnostic (no JPA annotations — those live on each service's own `OutboxEventEntity` in Phase 1+) representation of a single outbox row, used by `OutboxRelay` to remain persistence-technology-agnostic.

**3. Responsibilities**
- Fields: `id` (UUID), `eventType` (String, the wire name), `aggregateId` (UUID), `payload` (String, pre-serialized JSON), `status` (`OutboxEventStatus`), `createdAt` (Instant).
- Must NOT carry any JPA/persistence annotation — this is the pure model `OutboxRelay` operates on; each service's adapter maps its own JPA entity to/from this type.

**4. Dependencies**
- Internal: `OutboxEventStatus`.
- External: `java.time.Instant`, `java.util.UUID`.

**5. Public API**
- Constructor/record with the fields above.
- No behavior methods — pure data.

**6. Internal Workflow**
- N/A.

**7. Engineering Considerations**
- Keeping `payload` as a pre-serialized JSON `String` here (rather than a generic `T`) is deliberate: `OutboxRelay` never needs to know the payload's Java type, only that it's a JSON string ready to publish — serialization happens once, at write time, in each service's own use case.

**8. Testing Strategy**
- N/A beyond compilation.

**9. Future Extension**
- None anticipated; this model is intentionally minimal.

---

### InboxEvent.java
**Relative Path:** `platform/common-kafka/src/main/java/com/paymentgateway/common/inbox/InboxEvent.java`

**1. File Overview**
- Module: common-kafka | Package: `common.inbox` | Layer: Shared kernel
- Doc Cross-Reference: `SYSTEM_DESIGN.md` §7 (Inbox Pattern)

**2. Purpose**
- A framework-agnostic representation of a single consumed-event dedupe record, the consumer-side counterpart to `OutboxEvent`.

**3. Responsibilities**
- Fields: `eventId` (UUID, the deduplication key), `consumerName` (String — a consumer may subscribe to multiple event types across services; this identifies which logical consumer processed it, relevant if a service ever runs more than one consumer group against the same topic), `processedAt` (Instant).

**4. Dependencies**
- Internal/External: `java.time.Instant`, `java.util.UUID`.

**5. Public API**
- Constructor/record with the fields above.

**6. Internal Workflow**
- N/A.

**7. Engineering Considerations**
- Uniqueness on `(eventId, consumerName)` is the actual dedup guarantee — enforced by each service's own database unique constraint in Phase 1+, not by this model.

**8. Testing Strategy**
- N/A beyond compilation.

**9. Future Extension**
- None anticipated.

---

### KafkaTopicsProperties.java
**Relative Path:** `platform/common-kafka/src/main/java/com/paymentgateway/common/config/KafkaTopicsProperties.java`

**1. File Overview**
- Module: common-kafka | Package: `common.config` | Layer: Infrastructure (externalized configuration)
- Doc Cross-Reference: `Event-Catalog.md` §5 (Topic Catalog)

**2. Purpose**
- Externalizes the platform's seven topic names (`merchant.events`, `vault.events`, `payment.events`, `ledger.events`, `acquirer.events`, `webhook.events`, `settlement.events`) as bound configuration properties, so no service ever hardcodes a topic-name string literal.

**3. Responsibilities**
- `@ConfigurationProperties`-bound fields, one per topic, defaulting to the documented names but overridable per environment.
- Must NOT contain partition count, replication factor, or retention settings — those are infrastructure/cluster-provisioning concerns (Kafka admin tooling / `infra/` configuration), not application-level properties.

**4. Dependencies**
- Internal/External: Spring Boot `@ConfigurationProperties`.

**5. Public API**
- Getters (or constructor-binding fields) for each topic name.

**6. Internal Workflow**
- Bound at startup from `application.yml`'s `kafka.topics.*` keys.

**7. Engineering Considerations**
- Consistency: every producer/consumer configuration (`KafkaProducerConfig`, `KafkaConsumerConfig`, and each service's own producer/consumer classes in later phases) references topic names exclusively through this class.

**8. Testing Strategy**
- Binding test asserting default values match the documented topic names when no override is supplied.

**9. Future Extension**
- A new topic (e.g. if a future service is added) is a new field here first.

---

### KafkaProducerConfig.java
**Relative Path:** `platform/common-kafka/src/main/java/com/paymentgateway/common/config/KafkaProducerConfig.java`

**1. File Overview**
- Module: common-kafka | Package: `common.config` | Layer: Infrastructure
- Doc Cross-Reference: `SYSTEM_DESIGN.md` §7 (Outbox Pattern), `Event-Catalog.md` §9 (Delivery Guarantees)

**2. Purpose**
- Shared Kafka producer factory configuration (idempotent producer enabled, JSON serialization of `EventEnvelope`, acks/retries tuned for reliability) every service's Outbox Relay publish path uses.

**3. Responsibilities**
- Configures `enable.idempotence=true`, `acks=all`, a JSON serializer for the envelope's `payload` string (already-serialized, so effectively a `StringSerializer` for the value, with the key typically the `aggregateId` for partition-key routing per `Event-Catalog.md` §3.1).
- Must NOT configure per-service topic-specific behavior (e.g. a custom partitioner per topic) — that would belong in a service-specific override, not this shared base.

**4. Dependencies**
- Internal: `KafkaTopicsProperties` (for validation/logging only, not required for the producer factory itself).
- External: Spring Kafka `ProducerFactory`/`KafkaTemplate`.

**5. Public API**
- `@Bean` method producing a shared `ProducerFactory`/`KafkaTemplate` configured as above.

**6. Internal Workflow**
- Builds producer properties map, constructs the factory/template bean.

**7. Engineering Considerations**
- Reliability: `acks=all` + idempotent producer is the platform-standard combination guaranteeing no broker-level duplicate writes from network-level retries, per `Event-Catalog.md` §9's restated platform-wide guarantee.

**8. Testing Strategy**
- Integration test (Testcontainers Kafka) verifying a produced message round-trips with expected key/value and no duplication under simulated retry.

**9. Future Extension**
- If message compression becomes relevant at scale, add it here as a producer property — no per-service change needed.

---

### KafkaConsumerConfig.java
**Relative Path:** `platform/common-kafka/src/main/java/com/paymentgateway/common/config/KafkaConsumerConfig.java`

**1. File Overview**
- Module: common-kafka | Package: `common.config` | Layer: Infrastructure
- Doc Cross-Reference: `Event-Catalog.md` §6 (Consumer Groups), §9 (Delivery Guarantees)

**2. Purpose**
- Shared Kafka consumer factory configuration (manual offset commit after successful Inbox-deduped processing, deserialization error handling) each service's event consumer classes build on.

**3. Responsibilities**
- Configures manual acknowledgment mode (offset committed only after the consuming service's local transaction — business write + Inbox record — succeeds, per `SYSTEM_DESIGN.md` §7's at-least-once guarantee), a deserialization error handler that routes an unparseable message to logging/alerting rather than crashing the consumer thread.
- Must NOT configure a specific consumer group ID — each service names its own consumer group per its own bounded context, supplied via that service's own `application.yml`, not hardcoded here.

**4. Dependencies**
- Internal: `KafkaTopicsProperties`.
- External: Spring Kafka `ConsumerFactory`/`ConcurrentKafkaListenerContainerFactory`.

**5. Public API**
- `@Bean` method producing a shared `ConsumerFactory`/listener container factory configured as above.

**6. Internal Workflow**
- Builds consumer properties map (manual ack mode, deserializer, error handler), constructs the factory bean.

**7. Engineering Considerations**
- Reliability: manual-ack-after-business-write is what makes the platform's Inbox dedupe guarantee actually hold — an auto-commit configuration would risk committing an offset before the corresponding business effect is durably recorded.

**8. Testing Strategy**
- Integration test verifying a consumer only commits its offset after the (test) business handler completes successfully, and that a handler exception leaves the offset uncommitted for redelivery.

**9. Future Extension**
- If a dead-letter-topic pattern is ever adopted (currently the platform explicitly does not use one, per `Event-Catalog.md` §8), this is where it would be introduced.

---

### OutboxRelay.java
**Relative Path:** `platform/common-kafka/src/main/java/com/paymentgateway/common/outbox/OutboxRelay.java`

**1. File Overview**
- Module: common-kafka | Package: `common.outbox` | Layer: Shared kernel / application-adjacent infrastructure
- Doc Cross-Reference: `SYSTEM_DESIGN.md` §7 (Outbox Pattern)

**2. Purpose**
- The single, reusable polling-and-publishing engine every service's own Outbox adapter delegates to, guaranteeing the exact same "read unpublished rows, publish, mark published" behavior platform-wide rather than seven independent re-implementations.

**3. Responsibilities**
- Defines a port interface, `OutboxEventStorePort` (in this same package), with methods each service's own persistence adapter implements: find a batch of `PENDING` rows, mark a row `PUBLISHED`, mark a row `FAILED` after exhausted attempts.
- On each invocation (triggered by `OutboxRelayScheduler`), fetches a bounded batch of pending rows via the port, publishes each to Kafka via the shared `KafkaProducerConfig`-provided template, and marks each row's outcome via the port.
- Must NOT itself contain any JPA/database-specific code — persistence is entirely behind `OutboxEventStorePort`, implemented per-service in Phase 1+.

**4. Dependencies**
- Internal: `OutboxEvent`, `OutboxEventStatus`, `EventEnvelope` (for deserializing the stored JSON payload back into a publishable message, or simply forwarding the pre-serialized string as the Kafka message value — the latter is simpler and avoids a redundant deserialize/reserialize round trip, and is the recommended approach).
- External: Spring `KafkaTemplate`.

**5. Public API**
- `OutboxEventStorePort` interface methods: `List<OutboxEvent> findPendingBatch(int batchSize)`; `void markPublished(UUID outboxEventId)`; `void markFailed(UUID outboxEventId, String reason)`.
- `int pollAndPublish(int batchSize)` — Purpose: the relay's core cycle; Parameters: how many rows to fetch per invocation; Return: count of rows successfully published; Exceptions: does not propagate individual publish failures (caught, logged, row marked `FAILED` or left `PENDING` for retry depending on failure classification) — only propagates a genuine infrastructure-level failure (e.g. the store port itself is unreachable) to the caller/scheduler.

**6. Internal Workflow**
- Fetch a bounded batch of `PENDING` rows via the store port.
- For each row: publish its pre-serialized payload to the topic derived from its `eventType` (via `KafkaTopicsProperties`' event-type-to-topic mapping, resolved by a small internal lookup), keyed by `aggregateId`.
- On successful publish acknowledgment, call `markPublished`.
- On a transient publish failure, leave the row `PENDING` (next poll cycle retries it) up to a bounded number of cycles; on repeated failure beyond that bound, call `markFailed` and emit an alert-worthy log/metric.

**7. Engineering Considerations**
- Reliability: this is the component that makes the platform's "no event is ever lost" guarantee concrete — a row is never removed from `PENDING` until Kafka has acknowledged it.
- Performance: batched polling with a partial index on `published=false`-equivalent (`status=PENDING`) is assumed at the persistence layer per every service's own database design docs — this class's `batchSize` parameter exists specifically to keep each poll cycle bounded and fast regardless of historical table volume.

**8. Testing Strategy**
- Unit test with a fake `OutboxEventStorePort` and a mocked Kafka template: successful batch publish marks all rows published; a simulated publish failure leaves the row pending; repeated failures beyond the bound mark it failed.
- Integration test (Testcontainers Kafka) verifying an end-to-end publish actually lands on the expected topic/partition.

**9. Future Extension**
- If per-event-type publish prioritization is ever needed, `findPendingBatch` could accept an ordering/priority hint — not implemented now (no demonstrated need).

---

### OutboxRelayScheduler.java
**Relative Path:** `platform/common-kafka/src/main/java/com/paymentgateway/common/outbox/OutboxRelayScheduler.java`

**1. File Overview**
- Module: common-kafka | Package: `common.outbox` | Layer: Infrastructure (scheduled trigger)
- Doc Cross-Reference: `SYSTEM_DESIGN.md` §7 (Outbox Pattern)

**2. Purpose**
- The thin, `@Scheduled`-annotated trigger that periodically invokes `OutboxRelay.pollAndPublish(...)`, kept separate from `OutboxRelay` itself so the relay's core logic remains framework-scheduling-agnostic and independently unit-testable.

**3. Responsibilities**
- Invokes `OutboxRelay.pollAndPublish(batchSize)` on a fixed-delay schedule, both values externalized as properties (not hardcoded).
- Must NOT contain any publishing or persistence logic itself — purely a timing trigger.

**4. Dependencies**
- Internal: `OutboxRelay`.
- External: Spring `@Scheduled`.

**5. Public API**
- Scheduled method `triggerRelay()` — Purpose: the `@Scheduled` entry point; Parameters: none; Return: void; Exceptions: none escape (delegates entirely to `OutboxRelay`, which already handles its own failure classification).

**6. Internal Workflow**
- On each tick, call `outboxRelay.pollAndPublish(configuredBatchSize)` and log the returned publish count at `INFO`.

**7. Engineering Considerations**
- Each service configures its own fixed-delay interval appropriate to its own publish-latency SLO (e.g. tighter for Payment Orchestrator's `payment.events`, looser for lower-urgency topics) via its own `application.yml`, without needing a different Java class.

**8. Testing Strategy**
- Unit test verifying the scheduled method calls `pollAndPublish` with the configured batch size.

**9. Future Extension**
- None anticipated; deliberately kept minimal per the Simplicity First principle.

---

### InboxDeduplicationService.java
**Relative Path:** `platform/common-kafka/src/main/java/com/paymentgateway/common/inbox/InboxDeduplicationService.java`

**1. File Overview**
- Module: common-kafka | Package: `common.inbox` | Layer: Shared kernel / application-adjacent infrastructure
- Doc Cross-Reference: `SYSTEM_DESIGN.md` §7 (Inbox Pattern)

**2. Purpose**
- The single, reusable "have I already processed this event" check every service's Kafka consumer calls before executing its business handler, the consumer-side counterpart to `OutboxRelay`.

**3. Responsibilities**
- Defines a port interface, `InboxEventStorePort`, with methods each service's persistence adapter implements: check-and-record an `eventId` as processed within the same local transaction as the business handler's own write.
- Must NOT execute the business handler itself — this service only answers "already processed? yes/no" and, on "no," records the `eventId`; the calling consumer class (per-service, later phases) is responsible for wrapping both the business write and this record call in one local transaction.

**4. Dependencies**
- Internal: `InboxEvent`.
- External: none beyond the port interface.

**5. Public API**
- `InboxEventStorePort` interface method: `boolean tryMarkProcessed(UUID eventId, String consumerName)` — Purpose: atomically check-and-insert; Parameters: the event's ID and the logical consumer name; Return: `true` if this call newly recorded it (caller should proceed with business processing), `false` if it was already recorded (caller should skip processing, since another delivery already handled it); Exceptions: implementation-specific (data-access exceptions propagate — a failure here must abort the whole transaction, since proceeding without a successful dedupe record would risk double-processing).

**6. Internal Workflow**
- Delegates directly to the injected `InboxEventStorePort`'s atomic check-and-insert (implemented per-service against a unique-constrained `inbox_event` table in Phase 1+, using an `INSERT ... ON CONFLICT DO NOTHING`-equivalent or a caught unique-constraint-violation pattern to achieve atomicity without a separate SELECT-then-INSERT race).

**7. Engineering Considerations**
- Concurrency: the atomicity of "check and record" must be a single database operation, not a read-then-write pair, to avoid a race between two concurrent deliveries of the same (redelivered) message.
- Correctness: this must run in the *same* local transaction as the business handler's own write (both committed together, or both rolled back together) — a detail each service's consumer class (Phase 1+) is responsible for wiring correctly; this class only exposes the atomic primitive.

**8. Testing Strategy**
- Unit test with a fake `InboxEventStorePort`: first call for a given `eventId` returns `true`; second call for the same ID returns `false`.
- Integration test simulating two concurrent redelivery attempts, asserting exactly one succeeds in marking processed.

**9. Future Extension**
- If a future need arises to expire old inbox records (mirroring `IdempotencyRecordCleanupJob`), a similar shared cleanup job could be added here following the same pattern — not implemented now, since inbox retention policy hasn't yet been specified in any service's own doc.

---

# Section 5: platform/common-observability

Dependency order: `TracingConfig` → `CorrelationIdFilter` → `MetricsConfig` → `CircuitBreakerMetricsBinder` → `StructuredLoggingFilter` → `SensitiveDataMaskingPatternLayout`.

### TracingConfig.java
**Relative Path:** `platform/common-observability/src/main/java/com/paymentgateway/common/tracing/TracingConfig.java`

**1. File Overview**
- Module: common-observability | Package: `common.tracing` | Layer: Infrastructure
- Doc Cross-Reference: `Observability.md` §4 (Tracing)

**2. Purpose**
- Shared OpenTelemetry SDK configuration (OTLP exporter setup, resource attributes identifying the service name) every service imports so tracing is configured identically platform-wide.

**3. Responsibilities**
- Configures the OTLP exporter endpoint (externalized property), sampling strategy (100% retention for error spans per `Observability.md` §4, configurable rate for success spans), and a `Resource` attribute set including the service's own name (read from that service's own `application.yml`, not hardcoded here).
- Must NOT configure span attributes for any specific business operation — that is each service's own instrumentation concern; this class only wires the SDK/exporter plumbing.

**4. Dependencies**
- Internal/External: OpenTelemetry SDK, OTLP exporter artifact.

**5. Public API**
- `@Bean` method(s) producing the configured `OpenTelemetry`/`Tracer` instance.

**6. Internal Workflow**
- Reads exporter endpoint and service-name properties, builds the SDK's tracer provider and resource, registers the OTLP span exporter.

**7. Engineering Considerations**
- Consistency: every service's trace ends up in the same backend with the same resource-attribute shape, which is what makes the platform-wide "one trace, one dashboard" troubleshooting workflow (per `Observability.md` §10) actually work.

**8. Testing Strategy**
- Smoke test asserting the configured `Tracer` bean is non-null and produces a span with the expected resource attributes.

**9. Future Extension**
- If the platform migrates to a different trace backend, only the exporter configuration here changes.

---

### CorrelationIdFilter.java
**Relative Path:** `platform/common-observability/src/main/java/com/paymentgateway/common/tracing/CorrelationIdFilter.java`

**1. File Overview**
- Module: common-observability | Package: `common.tracing` | Layer: Infrastructure (Servlet/WebFlux filter)
- Doc Cross-Reference: `API-Standards.md` §4 (Headers, `X-Correlation-Id`), `Observability.md` §7

**2. Purpose**
- Shared filter that ensures every request has a correlation ID — generating one via `CorrelationIdGenerator` (common-core) if the inbound request didn't supply one — and propagates it onto both the outbound response and the logging MDC context for the duration of the request.

**3. Responsibilities**
- Reads `HeaderNames.X_CORRELATION_ID` from the inbound request; if absent, generates a new one.
- Places the resolved correlation ID into the logging MDC (so every log line during this request automatically includes it, per `Observability.md` §2) and into the reactive context if the service is WebFlux-based.
- Sets the same value on the outbound response header, so a caller always learns the correlation ID even if they didn't supply one.
- Must NOT overwrite a caller-supplied correlation ID under any circumstance — an inbound value is always passed through unchanged.

**4. Dependencies**
- Internal: `CorrelationIdGenerator`, `HeaderNames` (both common-core).
- External: Servlet `Filter` or WebFlux `WebFilter`, SLF4J MDC.

**5. Public API**
- Filter's standard `doFilter`/`filter` method — Purpose: the filter chain entry point; Parameters: request/response/chain per the framework's filter contract; Return: void/`Mono<Void>`; Exceptions: propagates any downstream exception unchanged (this filter never swallows an error).

**6. Internal Workflow**
- Resolve or generate the correlation ID.
- Set MDC key before invoking the rest of the chain; clear MDC after the chain completes (in a `finally`) to prevent leakage across pooled threads.
- Set the response header before or after chain completion depending on framework specifics.

**7. Engineering Considerations**
- Thread safety: MDC is thread-local: for a WebFlux/reactive stack, MDC alone is insufficient across async boundaries — the reactive context propagation path must also be wired (Reactor `Context` + a hook to bridge into MDC at log-emission time), a detail to resolve consistently at implementation time based on which services are WebFlux vs. Servlet per their own specs.

**8. Testing Strategy**
- Unit/integration test: request without correlation header receives a generated one on the response; request with a supplied header echoes it unchanged; MDC is populated during the request and cleared afterward.

**9. Future Extension**
- None anticipated; this is a stable, foundational cross-cutting filter.

---

### MetricsConfig.java
**Relative Path:** `platform/common-observability/src/main/java/com/paymentgateway/common/metrics/MetricsConfig.java`

**1. File Overview**
- Module: common-observability | Package: `common.metrics` | Layer: Infrastructure
- Doc Cross-Reference: `Observability.md` §3 (Metrics), §8 (Metrics Table)

**2. Purpose**
- Shared Micrometer registry customization (common tags such as `service`, `environment`) applied identically across every service so metrics are queryable/dashboardable with the same label scheme platform-wide.

**3. Responsibilities**
- Registers a `MeterRegistryCustomizer` adding common tags to every metric emitted by the importing service.
- Must NOT define any service-specific metric itself — each service's own components (e.g. `vault_tokenize_latency_seconds`) register their own metrics; this class only ensures they all carry the same baseline tag set.

**4. Dependencies**
- Internal/External: Micrometer `MeterRegistry`/`MeterRegistryCustomizer`.

**5. Public API**
- `@Bean` method producing a `MeterRegistryCustomizer<MeterRegistry>` that adds the common tags.

**6. Internal Workflow**
- Reads the service name/environment from that service's own configuration and applies them as common tags at registry-customization time (runs once at startup).

**7. Engineering Considerations**
- Consistency: this is what makes a single Grafana dashboard filterable by `service` label across all seven services without each one inventing its own tag key.

**8. Testing Strategy**
- Smoke test asserting a metric recorded after this customizer is applied carries the expected common tags.

**9. Future Extension**
- Additional common tags (e.g. `region` for future multi-region deployment) are added here once, benefiting every service simultaneously.

---

### CircuitBreakerMetricsBinder.java
**Relative Path:** `platform/common-observability/src/main/java/com/paymentgateway/common/metrics/CircuitBreakerMetricsBinder.java`

**1. File Overview**
- Module: common-observability | Package: `common.metrics` | Layer: Infrastructure
- Doc Cross-Reference: `Observability.md` §8 (`*_circuit_breaker_state{dependency}` gauge, described as "the one metric shape repeated identically across all seven components")

**2. Purpose**
- Binds every Resilience4j `CircuitBreaker` instance registered in a service's `CircuitBreakerRegistry` to a Micrometer gauge, so the platform-wide `*_circuit_breaker_state{dependency}` metric shape is produced automatically rather than each service wiring this binding by hand.

**3. Responsibilities**
- On application startup (and on new circuit breaker registration, if the registry supports listeners), binds each circuit breaker's current state (closed/open/half-open, represented as 0/1/2 per `API-Gateway-Part-02.md` §23's state-diagram convention) to a gauge tagged by the dependency name.
- Must NOT create or configure the circuit breakers themselves — each service's own `ResilienceConfig` defines its circuit breakers per-dependency; this class only observes and exposes their state.

**4. Dependencies**
- Internal/External: Resilience4j `CircuitBreakerRegistry`, Micrometer `MeterRegistry`.

**5. Public API**
- A registration/initialization method (e.g. invoked from a `@PostConstruct` or a `Bean` initialization callback) — Purpose: wire the binding; Parameters: the service's `CircuitBreakerRegistry` and `MeterRegistry`; Return: void; Exceptions: none expected under normal operation.

**6. Internal Workflow**
- Iterate all circuit breakers currently in the registry.
- For each, register a gauge reading its current state, tagged with the circuit breaker's name (which each service names after the dependency it protects, e.g. `token-vault`, `acquiring-provider-a`).
- If the registry supports a listener for dynamically-added circuit breakers, register newly-added ones automatically rather than requiring a restart.

**7. Engineering Considerations**
- This directly powers the "Dependency circuit open" alert class described in `Observability.md` §6, shared identically across every service.

**8. Testing Strategy**
- Unit test with a test circuit breaker registry: transitioning a breaker's state updates the corresponding gauge value.

**9. Future Extension**
- None anticipated; a stable, mechanical binding.

---

### StructuredLoggingFilter.java
**Relative Path:** `platform/common-observability/src/main/java/com/paymentgateway/common/logging/StructuredLoggingFilter.java`

**1. File Overview**
- Module: common-observability | Package: `common.logging` | Layer: Infrastructure
- Doc Cross-Reference: `Observability.md` §2 (Logging Standards)

**2. Purpose**
- Ensures the baseline log fields required by `Observability.md` §7 (`traceId`, `route`, `status`, `latencyMs`) are populated into the MDC/log context for every request, complementing `CorrelationIdFilter`'s correlation-ID-specific responsibility.

**3. Responsibilities**
- Records request start time, resolves `route` (matched path pattern, not raw URI with path variables interpolated, to keep metric/log cardinality bounded), and on response completion computes `latencyMs` and records `status`, placing all into MDC for the access-log line.
- Must NOT itself write the access-log line — that remains the responsibility of the standard access-logging mechanism (e.g. Logback's own request logging or a dedicated access-log statement each service's filter chain already produces); this class only ensures the *fields* are available when that line is emitted.

**4. Dependencies**
- Internal: works alongside `CorrelationIdFilter` and `TracingConfig`'s trace context.
- External: Servlet `Filter`/WebFlux `WebFilter`, SLF4J MDC.

**5. Public API**
- Filter's standard `doFilter`/`filter` method — Purpose: populate timing/route/status fields; Parameters/Return: per framework filter contract; Exceptions: propagates downstream exceptions unchanged, but still records `latencyMs`/`status` for the error response in a `finally` block.

**6. Internal Workflow**
- Record start timestamp before invoking the rest of the chain.
- After the chain completes (success or error), compute elapsed time, resolve final response status, place both into MDC, clear MDC afterward.

**7. Engineering Considerations**
- Must run in the correct filter order relative to `CorrelationIdFilter` (correlation ID should be established first, since it's referenced by the same log lines this filter's fields feed into) — the exact `@Order` value is an implementation detail resolved when both filters are wired into a specific service's filter chain.

**8. Testing Strategy**
- Integration test asserting a request's resulting log output (or MDC snapshot) contains `route`, `status`, and a plausible `latencyMs` value.

**9. Future Extension**
- Additional standard fields (if the platform's log schema grows) are added here once, benefiting every service.

---

### SensitiveDataMaskingPatternLayout.java
**Relative Path:** `platform/common-observability/src/main/java/com/paymentgateway/common/logging/SensitiveDataMaskingPatternLayout.java`

**1. File Overview**
- Module: common-observability | Package: `common.logging` | Layer: Infrastructure
- Doc Cross-Reference: `Token-Vault-Part-03.md` §38.7 (Sensitive Data Masking & PCI-Compliant Logging), applied platform-wide per `Observability.md` §2

**2. Purpose**
- A shared Logback pattern-layout/converter that scans outbound log lines for cardholder-data-shaped content (Luhn-valid 12–19 digit numeric runs) and redacts it, serving as the platform-wide last-resort safety net beneath each service's own structural type-level exclusion of sensitive fields from logging.

**3. Responsibilities**
- Applies a regex/Luhn-check-based scan to the rendered log message and redacts any matching sequence before the line is written to any appender.
- Must NOT be relied upon as the *primary* defense against cardholder-data logging — per `Token-Vault-Part-03.md` §38.7, the primary defense is structural (sensitive types simply have no loggable `toString()`); this class is explicitly the defense-in-depth layer, never the only layer.

**4. Dependencies**
- Internal/External: Logback `PatternLayout` extension point.

**5. Public API**
- Overridden layout/conversion method — Purpose: transform the about-to-be-written log line; Parameters: the logging event; Return: the (possibly redacted) formatted string; Exceptions: must never throw — a masking failure must fail safe (redact aggressively or drop the line) rather than let an unredacted sensitive value through due to an exception bypassing the masking step.

**6. Internal Workflow**
- Render the log line via the normal pattern layout.
- Scan the rendered string for Luhn-valid digit runs of plausible card-number length.
- Replace any match with a fixed redaction marker (e.g. `[REDACTED]`) before returning.

**7. Engineering Considerations**
- Performance: this runs on every log line platform-wide, so the regex/Luhn-check implementation must be efficient enough not to become a logging-throughput bottleneck at 10,000+ TPS — a compiled, single-pass regex is expected.
- Fail-safe design (per Responsibilities) is the single most important property of this class.

**8. Testing Strategy**
- Unit tests: a log line containing a Luhn-valid card-shaped number is redacted; a log line containing a non-Luhn-valid similar-length number is left alone (avoiding over-redaction that would harm log usefulness); a line causing an internal error during masking still produces a safe (redacted-by-default) output rather than propagating the raw line.

**9. Future Extension**
- If additional sensitive-data shapes need catching (beyond PAN-like sequences), extend the scan patterns here — this remains the single place such patterns are maintained.

---

# Section 6: platform/common-test-support

Dependency order: `PostgresTestContainerBase` → `KafkaTestContainerBase` → `RedisTestContainerBase` → `WireMockSupport` → `EventEnvelopeFixtures`.

### PostgresTestContainerBase.java
**Relative Path:** `platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/PostgresTestContainerBase.java`

**1. File Overview**
- Module: common-test-support | Package: `common.testcontainers` | Layer: Test infrastructure
- Doc Cross-Reference: `Coding-Guidelines.md` §8 (Testing Rules)

**2. Purpose**
- An abstract base class every service's integration tests extend to get a shared, singleton-per-test-run PostgreSQL Testcontainers instance, avoiding each service's test suite spinning up and tearing down its own container redundantly within the same build.

**3. Responsibilities**
- Starts a single static `PostgreSQLContainer` shared across all test classes extending this base within one JVM/test run, registering its JDBC URL/credentials as Spring test properties.
- Must NOT run Flyway migrations itself — each service's own test configuration triggers its own migrations against the shared container's database (each test class typically uses its own schema/database name within the shared container instance, or the container is parameterized per-service — resolved at implementation time based on whether services share one container instance across the whole reactor test run or one per-module).

**4. Dependencies**
- Internal/External: Testcontainers PostgreSQL module, JUnit 5 (`@Testcontainers`/`@Container` or manual static lifecycle management).

**5. Public API**
- Protected static container instance accessible to subclasses.
- A static initializer or `@DynamicPropertySource`-style method — Purpose: register the running container's connection properties into the Spring test context; Parameters/Return: per the chosen mechanism's contract.

**6. Internal Workflow**
- Container starts once (static, lazy) on first use across the test JVM.
- Subsequent test classes reuse the already-running instance.
- Container is not explicitly stopped by test code (Testcontainers' Ryuk resource reaper handles cleanup after the JVM exits) — a standard, accepted Testcontainers pattern for shared containers.

**7. Engineering Considerations**
- Test isolation: since the container is shared, each service's test suite must ensure it uses its own schema/database name (or truncates its own tables between test classes) to avoid cross-service or cross-test-class data bleed.

**8. Testing Strategy**
- N/A (this is itself test infrastructure); its correctness is implicitly verified by every service's own integration test suite running successfully against it.

**9. Future Extension**
- If a service ever needs a Postgres extension/version different from the shared default, that service can override with its own container rather than modifying this shared base.

---

### KafkaTestContainerBase.java
**Relative Path:** `platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/KafkaTestContainerBase.java`

**1. File Overview**
- Module: common-test-support | Package: `common.testcontainers` | Layer: Test infrastructure
- Doc Cross-Reference: `Coding-Guidelines.md` §8 (Testing Rules)

**2. Purpose**
- Analogous to `PostgresTestContainerBase`, but for a shared Kafka Testcontainers instance, used by every service's Outbox/Inbox integration tests.

**3. Responsibilities**
- Starts a single static Kafka container shared across the test run, registering its bootstrap-servers address as a Spring test property.
- Must NOT pre-create the platform's topics itself — either relies on broker auto-topic-creation (acceptable for test purposes) or a small shared topic-initialization helper invoked once per test run.

**4. Dependencies**
- Internal/External: Testcontainers Kafka module.

**5. Public API**
- Protected static container instance; a property-registration mechanism analogous to the Postgres base.

**6. Internal Workflow**
- Same shared-singleton-container pattern as `PostgresTestContainerBase`.

**7. Engineering Considerations**
- Test isolation: each test class should use a distinct consumer-group ID to avoid cross-test-class consumer rebalancing interference within the shared broker.

**8. Testing Strategy**
- N/A (test infrastructure itself).

**9. Future Extension**
- None anticipated.

---

### RedisTestContainerBase.java
**Relative Path:** `platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/RedisTestContainerBase.java`

**1. File Overview**
- Module: common-test-support | Package: `common.testcontainers` | Layer: Test infrastructure
- Doc Cross-Reference: `Coding-Guidelines.md` §8 (Testing Rules)

**2. Purpose**
- Analogous shared-container base for Redis, used by every service's cache/idempotency/rate-limit integration tests.

**3. Responsibilities**
- Starts a single static Redis container shared across the test run, registering host/port as Spring test properties.
- Must NOT assume any specific Redis Cluster/Sentinel topology — the shared test container is a single standalone instance, sufficient for testing application-level cache-aside/idempotency logic without needing to test Redis's own HA behavior (that is validated separately, if at all, via infrastructure-level testing, not application unit/integration tests).

**4. Dependencies**
- Internal/External: Testcontainers Redis (or generic container) module.

**5. Public API**
- Protected static container instance; property-registration mechanism.

**6. Internal Workflow**
- Same shared-singleton-container pattern.

**7. Engineering Considerations**
- Test isolation: tests should use distinct key prefixes or flush the relevant keys between test classes to avoid cross-test interference.

**8. Testing Strategy**
- N/A (test infrastructure itself).

**9. Future Extension**
- None anticipated.

---

### WireMockSupport.java
**Relative Path:** `platform/common-test-support/src/main/java/com/paymentgateway/common/wiremock/WireMockSupport.java`

**1. File Overview**
- Module: common-test-support | Package: `common.wiremock` | Layer: Test infrastructure
- Doc Cross-Reference: `Acquiring-Adapter-Part-04.md` §39 (Mock Provider Testing), `Token-Vault-Part-04.md` §50.2 (Integration Testing — WireMock-class HSM/KMS simulation)

**2. Purpose**
- A shared helper for starting/stopping a WireMock server instance and configuring stub responses, used by any service's integration tests that need to simulate an external HTTP dependency (a provider sandbox, a banking system, an HSM/KMS stub) without a real network call.

**3. Responsibilities**
- Starts a WireMock server on a dynamic port, exposes its base URL for test configuration, and provides convenience methods for registering common stub patterns (fixed response, delayed response for timeout simulation, sequential responses for retry-then-succeed scenarios).
- Must NOT hardcode any service-specific stub scenario — this class is a generic WireMock lifecycle/convenience wrapper; each service's own test classes define their own specific stubs using it.

**4. Dependencies**
- Internal/External: WireMock library.

**5. Public API**
- `start()` / `stop()` — lifecycle methods; Purpose: manage the embedded server's lifecycle; Parameters: none; Return: void/the running instance's base URL; Exceptions: propagates a startup failure (e.g. port binding issue) as an unchecked exception, since a test that cannot start its mock dependency cannot meaningfully run.
- `stubTimeout(String path, Duration delay)` — Purpose: convenience for timeout-scenario tests; Parameters: the stubbed path and delay; Return: void.
- `stubSequential(String path, List<ResponseDefinition>)` — Purpose: convenience for retry-then-succeed scenario tests; Parameters: the path and ordered response sequence; Return: void.

**6. Internal Workflow**
- Standard WireMock server lifecycle management; convenience methods are thin wrappers over WireMock's own stub-registration API.

**7. Engineering Considerations**
- Test isolation: each test class starts its own WireMock instance on a dynamic port (not shared across the whole test run, unlike the database/Kafka/Redis containers) since stub configurations are inherently test-case-specific and should not leak between test classes.

**8. Testing Strategy**
- N/A (test infrastructure itself); its correctness is implicitly verified by every service's own mock-provider integration tests.

**9. Future Extension**
- None anticipated; a stable, generic wrapper.

---

### EventEnvelopeFixtures.java
**Relative Path:** `platform/common-test-support/src/main/java/com/paymentgateway/common/fixtures/EventEnvelopeFixtures.java`

**1. File Overview**
- Module: common-test-support | Package: `common.fixtures` | Layer: Test infrastructure
- Doc Cross-Reference: `Event-Catalog.md` §6 (Event Catalog)

**2. Purpose**
- A shared test-data builder producing representative `EventEnvelope` instances for common event types, so every service's contract/integration tests constructing sample events don't each reinvent slightly-different sample data.

**3. Responsibilities**
- Provides static factory methods returning a plausible, valid `EventEnvelope<T>` for a small set of commonly-referenced-in-tests event types (e.g. a sample `MerchantActivated` envelope, a sample `PaymentCaptured` envelope) with realistic but clearly-fake field values.
- Must NOT be used as a substitute for each service's own domain-specific test fixtures for its own aggregates — this class only covers the generic envelope-construction convenience, not service-specific payload business data.

**4. Dependencies**
- Internal: `EventEnvelope`, `EventType` (both common-kafka).
- External: none.

**5. Public API**
- Static factory methods, e.g. `static EventEnvelope<Object> sampleMerchantActivated()`, following a consistent naming convention (`sample<EventName>()`) for each covered event type; Purpose: quick, valid test data; Parameters: none (or minimal overrides where a test needs to vary one field); Return: a populated `EventEnvelope`.

**6. Internal Workflow**
- Pure construction logic using `EventEnvelope.newEvent(...)` with fixed sample field values.

**7. Engineering Considerations**
- Keeping fixtures here (rather than duplicated per-service) is what prevents subtly-inconsistent sample data from making a contract test pass against one service's expectation but fail against another's.

**8. Testing Strategy**
- N/A (this class *is* test support); implicitly exercised by every test that uses it.

**9. Future Extension**
- New sample factories are added here as new event types become relevant to cross-service contract testing, following the established naming convention.

---

# Section 7: Build Verification

## What `mvn clean install` Must Verify After Phase 0

Running `mvn clean install` from the repository root must succeed and, in doing so, verify the following, in this order:

1. **Reactor resolution** — the root `pom.xml`'s `<modules>` list resolves correctly and Maven builds `platform/common-core`, `platform/common-security`, `platform/common-kafka`, `platform/common-observability`, and `platform/common-test-support` in valid dependency order (Maven determines actual build order from each module's own `<dependencies>`, but no module in this phase should declare a dependency that creates a cycle).
2. **Compilation** — every Java 21 source file listed in Sections 2–6 compiles cleanly under the Java 21 language level configured in the root POM, with zero warnings treated as errors per the enforcer configuration (if configured to fail-on-warning).
3. **No business logic present** — Phase 0's scope is exclusively shared kernel/infrastructure code; the build (and a manual review pass) should confirm no file in this phase contains a reference to any service-specific domain concept (e.g. no `Merchant`, `Token`, `Payment` type appears anywhere in `platform/`), consistent with these modules' purpose as genuinely shared, service-agnostic infrastructure.
4. **Unit tests pass** — every unit test described per-file above (`ErrorCode` default-status test, `BaseException` details-list test, `Result.getOrThrow()` branch tests, `CorrelationIdGenerator` uniqueness test, `IdempotencyKeyValidator` format tests, `JwksKeyCache`/`JwtValidator` mocked-scenario tests, `OutboxRelay`/`InboxDeduplicationService` fake-port tests, `SensitiveDataMaskingPatternLayout` redaction tests, etc.) passes under Surefire.
5. **Integration tests pass** — the Testcontainers-backed tests described above (`KafkaProducerConfig`/`KafkaConsumerConfig` round-trip tests) pass under Failsafe against real, ephemeral Postgres/Kafka containers, confirming the shared configuration classes actually produce working Spring beans, not just compiling code.
6. **Artifact installation** — each `platform/*` module's JAR is installed into the local Maven repository (`~/.m2`), making it resolvable as a dependency by Phase 1's service modules once those are added to the reactor.
7. **No premature service modules** — the reactor at the end of Phase 0 contains only the parent POM and the five `platform/*` modules; none of the seven services or `provider-simulator` should yet be buildable members of the reactor (they are added module-by-module starting in Phase 1), confirming Phase 0's scope boundary was respected.

A successful `mvn clean install` run satisfying all seven points above is the sole, objective definition of "Phase 0 complete" — no service-specific verification belongs at this phase.

---

# Phase 0 Completion Checklist

- [ ] Parent project configured (root `pom.xml` with correct `<modules>`, `<dependencyManagement>`, `<pluginManagement>`, Java 21 compiler level)
- [ ] Multi-module build working (`mvn clean install` resolves and builds all five `platform/*` modules in valid order)
- [ ] Shared libraries completed:
  - [ ] `platform/common-core` (10 files: `ErrorCode`, `BaseException`, `GlobalErrorAttributes`, `ErrorResponse`, `Result`, `HeaderNames`, `ScopeConstants`, `CorrelationIdGenerator`, `IdempotencyKeyValidator`, `IdempotencyRecordCleanupJob`)
  - [ ] `platform/common-security` (6 files: `WorkloadIdentity`, `MtlsIdentityExtractor`, `MtlsAllowListProperties`, `JwksKeyCache`, `JwtValidator`, `SecurityBaseConfig`)
  - [ ] `platform/common-kafka` (11 files: `EventType`, `EventEnvelope`, `OutboxEventStatus`, `OutboxEvent`, `InboxEvent`, `KafkaTopicsProperties`, `KafkaProducerConfig`, `KafkaConsumerConfig`, `OutboxRelay`, `OutboxRelayScheduler`, `InboxDeduplicationService`)
  - [ ] `platform/common-observability` (6 files: `TracingConfig`, `CorrelationIdFilter`, `MetricsConfig`, `CircuitBreakerMetricsBinder`, `StructuredLoggingFilter`, `SensitiveDataMaskingPatternLayout`)
  - [ ] `platform/common-test-support` (5 files: `PostgresTestContainerBase`, `KafkaTestContainerBase`, `RedisTestContainerBase`, `WireMockSupport`, `EventEnvelopeFixtures`)
- [ ] No business logic exists anywhere in `platform/` (no `Merchant`, `Token`, `Payment`, or any other service-domain concept referenced)
- [ ] Every Javadoc block per the Documentation & Learning Standard is present on every file, including its Doc Cross-Reference
- [ ] All modules compile successfully under Java 21
- [ ] All unit tests pass
- [ ] All Testcontainers-backed integration tests pass
- [ ] `mvn clean install` passes cleanly from the repository root with zero manual intervention
- [ ] Reactor contains only the parent POM + 5 `platform/*` modules — no Phase 1 service module has been added yet
- [ ] Ready for Phase 1 (Merchant Service domain/port/adapter slices, per the established build order)