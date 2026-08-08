## RedisTestContainerBase.java
Relative Path: platform/common-test-support/src/main/java/com/paymentgateway/common/testcontainers/RedisTestContainerBase.java

1. File Overview

Module: common-test-support | Package: common.testcontainers | Layer: Test infrastructure
Doc Cross-Reference: Coding-Guidelines.md §8 (Testing Rules)
2. Purpose

Analogous shared-container base for Redis, used by every service's cache/idempotency/rate-limit integration tests.
3. Responsibilities

Starts a single static Redis container shared across the test run, registering host/port as Spring test properties.
Must NOT assume any specific Redis Cluster/Sentinel topology — the shared test container is a single standalone instance, sufficient for testing application-level cache-aside/idempotency logic without needing to test Redis's own HA behavior (that is validated separately, if at all, via infrastructure-level testing, not application unit/integration tests).
4. Dependencies

Internal/External: Testcontainers Redis (or generic container) module.
5. Public API

Protected static container instance; property-registration mechanism.
6. Internal Workflow

Same shared-singleton-container pattern.
7. Engineering Considerations

Test isolation: tests should use distinct key prefixes or flush the relevant keys between test classes to avoid cross-test interference.
8. Testing Strategy

N/A (test infrastructure itself).
9. Future Extension

None anticipated.
## WireMockSupport.java
Relative Path: platform/common-test-support/src/main/java/com/paymentgateway/common/wiremock/WireMockSupport.java

1. File Overview

Module: common-test-support | Package: common.wiremock | Layer: Test infrastructure
Doc Cross-Reference: Acquiring-Adapter-Part-04.md §39 (Mock Provider Testing), Token-Vault-Part-04.md §50.2 (Integration Testing — WireMock-class HSM/KMS simulation)
2. Purpose

A shared helper for starting/stopping a WireMock server instance and configuring stub responses, used by any service's integration tests that need to simulate an external HTTP dependency (a provider sandbox, a banking system, an HSM/KMS stub) without a real network call.
3. Responsibilities

Starts a WireMock server on a dynamic port, exposes its base URL for test configuration, and provides convenience methods for registering common stub patterns (fixed response, delayed response for timeout simulation, sequential responses for retry-then-succeed scenarios).
Must NOT hardcode any service-specific stub scenario — this class is a generic WireMock lifecycle/convenience wrapper; each service's own test classes define their own specific stubs using it.
4. Dependencies

Internal/External: WireMock library.
5. Public API

start() / stop() — lifecycle methods; Purpose: manage the embedded server's lifecycle; Parameters: none; Return: void/the running instance's base URL; Exceptions: propagates a startup failure (e.g. port binding issue) as an unchecked exception, since a test that cannot start its mock dependency cannot meaningfully run.
stubTimeout(String path, Duration delay) — Purpose: convenience for timeout-scenario tests; Parameters: the stubbed path and delay; Return: void.
stubSequential(String path, List<ResponseDefinition>) — Purpose: convenience for retry-then-succeed scenario tests; Parameters: the path and ordered response sequence; Return: void.
6. Internal Workflow

Standard WireMock server lifecycle management; convenience methods are thin wrappers over WireMock's own stub-registration API.
7. Engineering Considerations

Test isolation: each test class starts its own WireMock instance on a dynamic port (not shared across the whole test run, unlike the database/Kafka/Redis containers) since stub configurations are inherently test-case-specific and should not leak between test classes.
8. Testing Strategy

N/A (test infrastructure itself); its correctness is implicitly verified by every service's own mock-provider integration tests.
9. Future Extension

None anticipated; a stable, generic wrapper.
## EventEnvelopeFixtures.java
Relative Path: platform/common-test-support/src/main/java/com/paymentgateway/common/fixtures/EventEnvelopeFixtures.java

1. File Overview

Module: common-test-support | Package: common.fixtures | Layer: Test infrastructure
Doc Cross-Reference: Event-Catalog.md §6 (Event Catalog)
2. Purpose

A shared test-data builder producing representative EventEnvelope instances for common event types, so every service's contract/integration tests constructing sample events don't each reinvent slightly-different sample data.
3. Responsibilities

Provides static factory methods returning a plausible, valid EventEnvelope<T> for a small set of commonly-referenced-in-tests event types (e.g. a sample MerchantActivated envelope, a sample PaymentCaptured envelope) with realistic but clearly-fake field values.
Must NOT be used as a substitute for each service's own domain-specific test fixtures for its own aggregates — this class only covers the generic envelope-construction convenience, not service-specific payload business data.
4. Dependencies

Internal: EventEnvelope, EventType (both common-kafka).
External: none.
5. Public API

Static factory methods, e.g. static EventEnvelope<Object> sampleMerchantActivated(), following a consistent naming convention (sample<EventName>()) for each covered event type; Purpose: quick, valid test data; Parameters: none (or minimal overrides where a test needs to vary one field); Return: a populated EventEnvelope.
6. Internal Workflow

Pure construction logic using EventEnvelope.newEvent(...) with fixed sample field values.
7. Engineering Considerations

Keeping fixtures here (rather than duplicated per-service) is what prevents subtly-inconsistent sample data from making a contract test pass against one service's expectation but fail against another's.
8. Testing Strategy

N/A (this class is test support); implicitly exercised by every test that uses it.
9. Future Extension

New sample factories are added here as new event types become relevant to cross-service contract testing, following the established naming convention.