Result.java
Relative Path: platform/common-core/src/main/java/com/paymentgateway/common/result/Result.java

1. File Overview

Module: common-core | Package: common.result | Layer: Shared kernel (application-layer helper)
Doc Cross-Reference: Coding-Guidelines.md §3 (Layer Responsibilities), §10 (Clean Architecture Rules)
2. Purpose

A generic success/failure wrapper (Java 21 sealed interface with two record implementations, Success<T> and Failure) available to any service's application-layer use case that prefers an explicit result type over throwing for expected, non-exceptional business outcomes (e.g. a validation-style rejection that isn't severe enough to warrant an exception-handler round trip).
Optional, not mandatory — most use cases in this platform throw BaseException subtypes per Coding-Guidelines.md §6, and Result exists only for the narrow cases (documented per-service) where a use case's caller needs to branch on outcome without exception-handling overhead.
3. Responsibilities

Success<T> wraps a value; Failure wraps an ErrorCode and message.
Must NOT be used as a substitute for the platform's exception-based error handling for anything crossing a controller boundary — Result is an internal, application-layer-only construct; controllers always translate to either a normal response or let a BaseException propagate to the handler.
4. Dependencies

Internal: ErrorCode.
External: none.
5. Public API

static <T> Result<T> success(T value) — Purpose: wrap a successful value; Return: Success<T>.
static <T> Result<T> failure(ErrorCode code, String message) — Purpose: wrap a failure; Return: Failure.
boolean isSuccess() / boolean isFailure() — accessors.
T getOrThrow() — Purpose: unwrap a Success, or throw a BaseException-derived exception carrying the wrapped ErrorCode/message if this is a Failure; Exceptions: throws on Failure.
6. Internal Workflow

Pattern-matched via Java 21 switch on the sealed type at the call site; no internal state machine.
7. Engineering Considerations

Thread safety: immutable records, inherently thread-safe.
8. Testing Strategy

Unit tests for getOrThrow() on both branches.
9. Future Extension

Could gain a map/flatMap combinator later if functional-style chaining becomes a recurring need — not implemented now per the Simplicity First principle (no speculative API surface).
HeaderNames.java
Relative Path: platform/common-core/src/main/java/com/paymentgateway/common/constant/HeaderNames.java

1. File Overview

Module: common-core | Package: common.constant | Layer: Shared kernel
Doc Cross-Reference: API-Standards.md §4 (Headers)
2. Purpose

Single source of truth for every platform-standard HTTP header name string, preventing typo-prone duplicated literals ("X-Correlation-Id" etc.) across seven services.
3. Responsibilities

Defines public static final String constants: AUTHORIZATION, X_API_KEY, IDEMPOTENCY_KEY, X_CORRELATION_ID, TRACEPARENT, TRACESTATE, X_MERCHANT_ID, CONTENT_TYPE, RETRY_AFTER.
Must NOT contain any header value or parsing logic — names only.
4. Dependencies

Internal/External: none.
5. Public API

Public static final String fields only; no methods (final utility class, private constructor to prevent instantiation).
6. Internal Workflow

N/A.
7. Engineering Considerations

N/A beyond immutability.
8. Testing Strategy

No behavior to test; compilation is the verification.
9. Future Extension

New platform-standard headers are added here first, before any service references the literal string.
ScopeConstants.java
Relative Path: platform/common-core/src/main/java/com/paymentgateway/common/constant/ScopeConstants.java

1. File Overview

Module: common-core | Package: common.constant | Layer: Shared kernel
Doc Cross-Reference: Security-Architecture.md §3 (Authorization)
2. Purpose

Single source of truth for the platform's authorization scope-string vocabulary (payments:read, payments:write, merchant:admin, etc.), shared between the API Gateway (coarse route-class checks) and Merchant Service (credential scope issuance/validation).
3. Responsibilities

Defines public static final String scope constants and, optionally, a Set<String> ALL_SCOPES for validation against unrecognized scope strings.
Must NOT encode which route requires which scope — that mapping is the API Gateway's own RouteDefinition/ScopePolicy configuration, not this class's concern.
4. Dependencies

Internal/External: none.
5. Public API

Public static final String fields; public static final Set<String> ALL_SCOPES.
6. Internal Workflow

N/A.
7. Engineering Considerations

N/A.
8. Testing Strategy

Unit test asserting ALL_SCOPES contains every declared constant (prevents a forgotten registration).
9. Future Extension

New scopes added here; Merchant Service's ScopeValidator (Phase 1) references ALL_SCOPES to reject unrecognized values.
CorrelationIdGenerator.java
Relative Path: platform/common-core/src/main/java/com/paymentgateway/common/util/CorrelationIdGenerator.java

1. File Overview

Module: common-core | Package: common.util | Layer: Shared kernel utility
Doc Cross-Reference: Observability.md §7 (Log Fields Table, correlationId)
2. Purpose

Generates a new correlation ID when an inbound request doesn't already carry one, used by CorrelationIdFilter (common-observability) across every service.
3. Responsibilities

Generates a UUIDv4 string suitable for cross-service correlation.
Must NOT attempt to validate or parse an existing incoming correlation ID's format — an upstream-supplied value is passed through unchanged regardless of its shape, per platform convention (never rejecting a caller's correlation ID).
4. Dependencies

Internal/External: java.util.UUID.
5. Public API

static String generate() — Purpose: produce a new correlation ID; Parameters: none; Return: UUIDv4 string; Exceptions: none.
6. Internal Workflow

Delegates directly to UUID.randomUUID().toString().
7. Engineering Considerations

Uses the JVM's default (non-cryptographic) UUID generator — sufficient here since a correlation ID is a tracing convenience, not a security token (unlike VaultTokenId in Token Vault, which has a materially different, security-relevant generation requirement specified in its own service).
8. Testing Strategy

Unit test asserting uniqueness across repeated calls and valid UUID format.
9. Future Extension

None anticipated; this is intentionally the simplest possible implementation.