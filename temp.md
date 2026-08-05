## JwtValidator.java
Relative Path: platform/common-security/src/main/java/com/paymentgateway/common/jwt/JwtValidator.java

1. File Overview

Module: common-security | Package: common.jwt | Layer: Infrastructure
Doc Cross-Reference: API-Gateway-Part-02.md §25.2 (JWT Validation)
2. Purpose

Validates an inbound Bearer JWT's signature, expiry, issuer, and audience per the platform's JWT authentication standard, used by the API Gateway (the platform's sole JWT-validating component per Security-Architecture.md §2).
3. Responsibilities

Validates signature using RS256/ES256 only (explicit algorithm allow-list — never trusts the token's own alg header without cross-checking against the configured allow-list, preventing algorithm-confusion attacks).
Validates exp, nbf (±60s clock skew tolerance), iss, aud claims against configured expected values.
Must NOT accept HS256 or any symmetric algorithm from an external caller under any configuration.
Must NOT perform authorization (scope checking) — this class answers only "is this token authentic and current," not "is this caller allowed to do X."
4. Dependencies

Internal: JwksKeyCache.
External: a JWT parsing library (e.g. Nimbus JOSE+JWT / java-jwt), consistent platform-wide once chosen here.
5. Public API

AuthenticatedClaims validate(String rawJwt) — Purpose: full validation pipeline; Parameters: the raw Bearer token string (without the "Bearer " prefix, stripped by the caller); Return: a small claims value object (subject, scopes, merchant/client identifier) on success; Exceptions: throws a specific unchecked exception (e.g. InvalidTokenException) with enough detail for internal logging but never enough to leak into an external response body (per API-Gateway-Part-02.md §19.11's generic-denial requirement).
6. Internal Workflow

Parse the JWT header to read alg and kid without yet trusting them.
Reject immediately if alg is not in the RS256/ES256 allow-list.
Resolve the public key via JwksKeyCache.getKey(kid).
Verify signature with that key.
Validate exp/nbf/iss/aud claims.
On full success, map claims into AuthenticatedClaims and return.
7. Engineering Considerations

Security: every rejection path (bad algorithm, bad signature, expired, wrong issuer/audience) must be logged with enough detail for security monitoring while the exception surfaced to the caller stays generic.
Performance: relies entirely on JwksKeyCache to avoid a remote call on the hot authentication path.
8. Testing Strategy

Unit tests: valid token, expired token, wrong issuer, wrong audience, HS256-signed token (must be rejected outright), tampered signature, clock-skew boundary cases (±60s).
9. Future Extension

If OpenID Connect discovery is adopted later, issuer/audience configuration could be resolved dynamically rather than statically configured — a change confined to this class and JwksKeyCache.
## SecurityBaseConfig.java
Relative Path: platform/common-security/src/main/java/com/paymentgateway/common/config/SecurityBaseConfig.java

1. File Overview

Module: common-security | Package: common.config | Layer: Infrastructure (Spring configuration)
Doc Cross-Reference: Security-Architecture.md §2–§3 (Authentication, Authorization)
2. Purpose

Provides the shared baseline Spring Security filter-chain configuration (stateless session policy, CSRF disabled for a pure API surface, common security headers) every service imports and extends with its own service-specific filters (e.g. AuthenticationFilter at the Gateway, InternalServiceAuthFilter elsewhere).
3. Responsibilities

Configures SessionCreationPolicy.STATELESS, disables CSRF (appropriate for a bearer-token/mTLS API platform with no cookie-based session), and registers common security headers (e.g. HSTS where applicable).
Must NOT register any service-specific filter itself — each service's own SecurityConfig composes this base with its own filters.
4. Dependencies

Internal/External: Spring Security (SecurityFilterChain / ServerHttpSecurity depending on Servlet vs. WebFlux stack per service).
5. Public API

A @Bean-producing method returning the base security configuration object each service's own SecurityConfig further customizes (exact return type — HttpSecurity/ServerHttpSecurity builder or a reusable customizer — resolved at implementation time based on the consuming service's stack).
6. Internal Workflow

Applies the baseline settings described in Responsibilities; delegates everything else to the importing service.
7. Engineering Considerations

Consistency: this is what guarantees no service accidentally reintroduces stateful sessions or leaves CSRF enabled on an API-only surface.
8. Testing Strategy

Each service's own security integration test (Phase 1+) verifies the composed chain behaves as expected; this base class itself only needs a smoke test confirming it produces a non-null, stateless configuration.
9. Future Extension

If a future service genuinely needs cookie-based sessions (unlikely, but e.g. an admin dashboard), that service overrides rather than modifies this shared base.

## EventType.java
Relative Path: platform/common-kafka/src/main/java/com/paymentgateway/common/envelope/EventType.java

1. File Overview

Module: common-kafka | Package: common.envelope | Layer: Shared kernel
Doc Cross-Reference: Event-Catalog.md §6 (Event Catalog table), §2 (Event Naming Convention)
2. Purpose

Enumerates every domain event name in the platform's Event Catalog as a closed, typo-proof vocabulary shared by every producer and consumer, rather than each service passing raw strings.
3. Responsibilities

One enum constant per event listed in Event-Catalog.md §6 (e.g. MERCHANT_ACTIVATED, TOKEN_CREATED, PAYMENT_CAPTURED, SETTLEMENT_COMPLETED, etc.) — the full list is authoritative in that document and must be reproduced exactly, not re-derived or abbreviated.
Must NOT encode which topic an event belongs to — that mapping lives in KafkaTopicsProperties, keeping event identity and topic routing independently configurable.
4. Dependencies

Internal/External: none.
5. Public API

Enum constants only, plus String wireName() — Purpose: the exact past-tense string used on the wire/in the EventEnvelope.eventType field (may equal name() or a explicitly mapped string if casing conventions differ); Return: String; Exceptions: none.
6. Internal Workflow

N/A (declarative).
7. Engineering Considerations

Any new event introduced in a later phase must be added here and cross-checked against Event-Catalog.md before that service's producer code is written — this file is intentionally kept in lock-step with the documentation.
8. Testing Strategy

A unit test cross-referencing this enum's constant count/names against a checked-in copy of the Event Catalog list (a simple safeguard against drift, not a live doc-parser).
9. Future Extension

Additive only; an event name is never renamed or removed once any service has shipped a producer/consumer for it (per Event-Catalog.md §10, Event Versioning).
## EventEnvelope.java
Relative Path: platform/common-kafka/src/main/java/com/paymentgateway/common/envelope/EventEnvelope.java

1. File Overview

Module: common-kafka | Package: common.envelope | Layer: Shared kernel
Doc Cross-Reference: SYSTEM_DESIGN.md §5 (Event Envelope), Event-Catalog.md §4 (Event Lifecycle Diagram)
2. Purpose

The single platform-wide event wrapper (Java 21 generic record) every service's Outbox row is serialized into and every Kafka message deserializes from, guaranteeing every event on every topic carries the same structural metadata.
3. Responsibilities

Fields, exactly per SYSTEM_DESIGN.md §5: eventId (UUID), eventType (EventType), aggregateId (UUID), version (long), correlationId (UUID), causationId (UUID, nullable), timestamp (Instant, UTC), payload (generic type T, the event-specific data).
Must NOT allow payload to be an untyped Object/Map in application code — each producer constructs EventEnvelope<SpecificPayloadType>, so payload shape is compile-time checked up to the serialization boundary.
Must NOT ever contain a field capable of holding cardholder data, secrets, or key material — this is a structural, platform-wide guarantee independent of any single service's own discipline.
4. Dependencies

Internal: EventType.
External: java.time.Instant, java.util.UUID, a JSON serialization library (Jackson, standard with Spring Boot) for the payload.
5. Public API

Canonical generic record constructor EventEnvelope<T>(UUID eventId, EventType eventType, UUID aggregateId, long version, UUID correlationId, UUID causationId, Instant timestamp, T payload).
Static factory static <T> EventEnvelope<T> newEvent(EventType type, UUID aggregateId, long version, UUID correlationId, UUID causationId, T payload) — Purpose: convenience construction generating a fresh eventId and current timestamp; Return: new envelope instance.
6. Internal Workflow

Pure data carrier; serialization/deserialization is handled by Jackson configuration in KafkaProducerConfig/KafkaConsumerConfig, not by this class itself.
7. Engineering Considerations

Generic type erasure: Jackson's generic deserialization requires either a TypeReference at the consumer side or a wrapping mechanism (e.g. carrying the payload's fully-qualified class name, or each consumer knowing its expected payload type upfront since it only ever subscribes to specific event types) — this detail must be resolved consistently in KafkaConsumerConfig and documented there.
8. Testing Strategy

Serialization round-trip test: construct an envelope with a sample payload type, serialize, deserialize, assert equality.
9. Future Extension

version supports future schema evolution per Event-Catalog.md §10; new optional payload fields are additive within T, never a structural change to the envelope itself.
