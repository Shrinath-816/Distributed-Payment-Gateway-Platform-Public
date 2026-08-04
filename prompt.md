# Service Parent POM Specifications

Document status: Implementation specification for the eight service-module `pom.xml` files. Each declares its module as a child of the root reactor and depends on exactly the `platform/*` shared libraries and external frameworks its own service specification requires — nothing speculative.

Order presented follows the platform's established build order: Merchant Service → Token Vault → Payment Orchestrator → Acquiring Adapter → Webhook Service → Settlement Service → API Gateway → Provider Simulator.

---

### merchant-service/pom.xml
**Relative Path:** `merchant-service/pom.xml`

**1. File Overview** — Module: merchant-service | Layer: Build configuration | Doc Cross-Reference: `Merchant-Service-Part-01.md` §26–29

**2. Purpose** — Declares the Merchant Service as a Spring Boot executable module (`packaging: jar`, Spring Boot Maven Plugin repackaging enabled), the platform's identity/lifecycle/credential system of record.

**3. Responsibilities**
- Depends on `common-core`, `common-security`, `common-kafka`, `common-observability` (production scope) and `common-test-support` (test scope).
- Declares Spring Boot starters for a Servlet-stack REST API with JPA persistence (per this service's lower-throughput, JDBC-acceptable profile), Redis caching, Kafka, validation, and OpenAPI documentation.
- Must NOT depend on any other service module directly — cross-service calls happen over HTTP/Kafka at runtime, never a compile-time module dependency.

**4. Dependencies**
- Internal: `common-core`, `common-security`, `common-kafka`, `common-observability`, `common-test-support` (test scope).
- External: `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-data-redis`, `spring-kafka` (already transitively via common-kafka, declared explicitly if the service needs it directly), `spring-boot-starter-validation`, `spring-boot-starter-actuator`, `flyway-core` + `flyway-database-postgresql`, `postgresql` (JDBC driver), `springdoc-openapi-starter-webmvc-ui`, Lombok, `spring-boot-starter-test` + Testcontainers (test scope).

**5. Public API** — N/A (build file).

**6. Internal Workflow** — Standard Maven module resolution; Spring Boot plugin produces an executable JAR at package phase.

**7. Engineering Considerations** — This is the first service module added to the reactor after Phase 0's platform libraries; its `pom.xml` is the template every subsequent service's POM structurally mirrors (same starter categories, same plugin configuration), varying only in which specific starters/drivers are needed.

**8. Testing Strategy** — Verified by `mvn clean install`; unit tests via Surefire, `src/it` integration tests via Failsafe against Testcontainers Postgres/Kafka/Redis.

**9. Future Extension** — Additional starters (e.g. a future R2DBC migration per the platform's still-open JDBC-vs-R2DBC ADR) are added here without affecting any other service's POM.

---

### token-vault-service/pom.xml
**Relative Path:** `token-vault-service/pom.xml`

**1. File Overview** — Module: token-vault-service | Layer: Build configuration | Doc Cross-Reference: `Token-Vault-Part-01.md` §26–28

**2. Purpose** — Declares the Token Vault as a reactive (WebFlux) Spring Boot module, reflecting its strict sub-30ms/sub-20ms latency budgets and non-blocking I/O requirement.

**3. Responsibilities**
- Depends on `common-core`, `common-security`, `common-kafka`, `common-observability` (production), `common-test-support` (test).
- Declares WebFlux (not Servlet/MVC) starters, R2DBC (not JPA) for both the operational and audit database connections, Redis reactive support, and a KMS/HSM client SDK dependency.
- Must NOT declare `spring-boot-starter-web` — this service is reactive end-to-end per its own architecture spec.

**4. Dependencies**
- Internal: `common-core`, `common-security`, `common-kafka`, `common-observability`, `common-test-support` (test scope).
- External: `spring-boot-starter-webflux`, `spring-boot-starter-data-r2dbc`, `r2dbc-postgresql`, `postgresql` (JDBC driver, used by Flyway which does not support R2DBC directly), `flyway-core` + `flyway-database-postgresql`, `spring-boot-starter-data-redis-reactive`, a KMS/HSM client artifact (vendor-neutral interface implementation, concrete SDK dependency added when the provider is selected per the platform's pending ADR), Lombok, `spring-boot-starter-test` + Testcontainers + Reactor Test (test scope).

**5. Public API** — N/A (build file).

**6. Internal Workflow** — Standard Maven module resolution.

**7. Engineering Considerations** — This is the only service module requiring two distinct database connection configurations (operational + isolated audit) at the dependency-declaration level — both driven by the same R2DBC/Flyway dependencies, distinguished at the `application.yml` configuration level, not by additional POM dependencies.

**8. Testing Strategy** — Verified by `mvn clean install`; reactive-stack tests use Reactor's `StepVerifier`; integration tests run against Testcontainers Postgres (both schemas) and a WireMock-based KMS/HSM stub.

**9. Future Extension** — Swapping KMS/HSM providers changes only this POM's vendor SDK dependency, per the vendor-neutral `KeyWrappingPort` abstraction.

---

### payment-orchestrator-service/pom.xml
**Relative Path:** `payment-orchestrator-service/pom.xml`

**1. File Overview** — Module: payment-orchestrator-service | Layer: Build configuration | Doc Cross-Reference: `Payment-Orchestrator-Part-01.md` §11–12

**2. Purpose** — Declares the Payment Orchestrator as a reactive Spring Boot module, reflecting its position as the platform's highest-throughput hot path (10,000+ TPS aggregate target).

**3. Responsibilities**
- Depends on `common-core`, `common-security`, `common-kafka`, `common-observability` (production), `common-test-support` (test).
- Declares WebFlux, R2DBC, Redis reactive, and Resilience4j (circuit breaker/retry/timeout/bulkhead) starters — this is the first service module needing Resilience4j declared explicitly, since it fans out to three downstream services (Merchant Service, Token Vault, Acquiring Adapter) per request.
- Must NOT declare a direct compile-time dependency on `merchant-service`, `token-vault-service`, or `acquiring-adapter-service` — all three are called over HTTP/mTLS at runtime via this service's own adapter classes, never as Maven module dependencies.

**4. Dependencies**
- Internal: `common-core`, `common-security`, `common-kafka`, `common-observability`, `common-test-support` (test scope).
- External: `spring-boot-starter-webflux`, `spring-boot-starter-data-r2dbc`, `r2dbc-postgresql`, `postgresql`, `flyway-core` + `flyway-database-postgresql`, `spring-boot-starter-data-redis-reactive`, `spring-boot-starter-webflux`'s `WebClient` (for the three downstream client adapters), `resilience4j-spring-boot3`, `resilience4j-reactor`, Lombok, `spring-boot-starter-test` + Testcontainers + Reactor Test + WireMock (test scope, for mocking the three downstream services in isolated tests).

**5. Public API** — N/A (build file).

**6. Internal Workflow** — Standard Maven module resolution.

**7. Engineering Considerations** — The explicit `resilience4j-reactor` dependency (rather than only the Servlet-stack `resilience4j-spring-boot3` integration) is required because this service's circuit breakers/retries wrap reactive `WebClient` calls, not blocking calls.

**8. Testing Strategy** — Verified by `mvn clean install`; the concurrent-duplicate-idempotency load test (proving no double-charge under simulated concurrent identical requests) runs as part of this module's `src/it` integration suite.

**9. Future Extension** — Adding a future fourth downstream dependency requires only a new `WebClient`-based adapter class and, if a new external artifact is needed for its protocol, a new dependency entry here.

---

### acquiring-adapter-service/pom.xml
**Relative Path:** `acquiring-adapter-service/pom.xml`

**1. File Overview** — Module: acquiring-adapter-service | Layer: Build configuration | Doc Cross-Reference: `Acquiring-Adapter-Part-01.md` §12–13

**2. Purpose** — Declares the Acquiring Adapter as a reactive Spring Boot module, the platform's sole component making outbound calls to external (simulated) acquirers/PSPs.

**3. Responsibilities**
- Depends on `common-core`, `common-security`, `common-kafka`, `common-observability` (production), `common-test-support` (test).
- Declares WebFlux, R2DBC, Resilience4j (per-connector circuit breakers), and per-provider authentication library dependencies (OAuth2 client-credentials support for one connector, HMAC verification support for another) needed across its four provider connectors.
- Must NOT declare a compile-time dependency on `provider-simulator` — the simulator is called over HTTP at runtime like any other external acquirer, exactly mirroring how a real acquirer would be integrated.

**4. Dependencies**
- Internal: `common-core`, `common-security`, `common-kafka`, `common-observability`, `common-test-support` (test scope).
- External: `spring-boot-starter-webflux`, `spring-boot-starter-data-r2dbc`, `r2dbc-postgresql`, `postgresql`, `flyway-core` + `flyway-database-postgresql`, `resilience4j-spring-boot3` + `resilience4j-reactor`, `spring-security-oauth2-client` (Provider Connector D's OAuth2 client-credentials flow), Lombok, `spring-boot-starter-test` + Testcontainers + Reactor Test + WireMock (test scope, essential here for simulating each connector's approve/decline/timeout/malformed-response scenarios per `Acquiring-Adapter-Part-04.md` §39).

**5. Public API** — N/A (build file).

**6. Internal Workflow** — Standard Maven module resolution.

**7. Engineering Considerations** — This POM's test-scope WireMock dependency carries more weight than in other services, since mock-provider testing is this service's primary integration-test strategy per its own testing section.

**8. Testing Strategy** — Verified by `mvn clean install`; per-connector unit tests plus WireMock-based integration tests covering every documented error-mapping scenario.

**9. Future Extension** — Onboarding a fifth real-world provider adds one new connector sub-package and, only if that provider's auth scheme isn't already covered (API key, OAuth2, HMAC, certificate), one new dependency here.

---

### webhook-service/pom.xml
**Relative Path:** `webhook-service/pom.xml`

**1. File Overview** — Module: webhook-service | Layer: Build configuration | Doc Cross-Reference: `Webhook-Service-Part-01.md` §11–13

**2. Purpose** — Declares the Webhook Service as a Spring Boot module whose bottleneck is I/O-wait on external merchant endpoints rather than CPU, reflected in its dependency set favoring non-blocking outbound HTTP delivery.

**3. Responsibilities**
- Depends on `common-core`, `common-security`, `common-kafka`, `common-observability` (production), `common-test-support` (test).
- Declares WebFlux (for non-blocking outbound delivery calls to arbitrary merchant endpoints), R2DBC, Redis reactive, Resilience4j, and a cryptography library sufficient for HMAC-SHA256 signing (covered by the JDK's own `javax.crypto`, requiring no additional external dependency beyond what Spring Boot already provides).
- Must NOT declare a compile-time dependency on `merchant-service` — webhook configuration is read via HTTP internal API call, never a shared JPA entity or module dependency.

**4. Dependencies**
- Internal: `common-core`, `common-security`, `common-kafka`, `common-observability`, `common-test-support` (test scope).
- External: `spring-boot-starter-webflux`, `spring-boot-starter-data-r2dbc`, `r2dbc-postgresql`, `postgresql`, `flyway-core` + `flyway-database-postgresql`, `spring-boot-starter-data-redis-reactive`, `resilience4j-spring-boot3` + `resilience4j-reactor`, Lombok, `spring-boot-starter-test` + Testcontainers + Reactor Test + WireMock (test scope, for simulating merchant endpoints returning 2xx/4xx/5xx/timeout/malformed responses).

**5. Public API** — N/A (build file).

**6. Internal Workflow** — Standard Maven module resolution.

**7. Engineering Considerations** — No JWT/OAuth2-specific security dependency is needed beyond what `common-security` already provides, since this service's only inbound surface is an internal operator API authenticated via mTLS + role, not JWT.

**8. Testing Strategy** — Verified by `mvn clean install`; the seven-attempt exponential backoff and Dead Letter Queue behavior are verified via WireMock-simulated persistently-failing endpoints in `src/it`.

**9. Future Extension** — If a future webhook-versioning capability is added, no new dependency is anticipated — it is a payload-shape change, not a new integration.

---

### settlement-service/pom.xml
**Relative Path:** `settlement-service/pom.xml`

**1. File Overview** — Module: settlement-service | Layer: Build configuration | Doc Cross-Reference: `Settlement-Service-Part-01.md` §9–10

**2. Purpose** — Declares the Settlement Service as a Spring Boot module whose workload is bursty and cutoff-triggered (batch cycle runs) rather than continuously request-driven, reflected in its scheduling-heavy dependency profile.

**3. Responsibilities**
- Depends on `common-core`, `common-security`, `common-kafka`, `common-observability` (production), `common-test-support` (test).
- Declares WebFlux/R2DBC (consistent with the platform's reactive-first services), Redis reactive, Resilience4j, and Spring's own scheduling support (already part of `spring-boot-starter`, no separate artifact needed) for the cycle-cutoff trigger.
- Must NOT declare a compile-time dependency on `merchant-service` or `payment-orchestrator-service` — payout account lookup and ledger facts arrive via internal HTTP call and Kafka consumption respectively, never a module dependency.

**4. Dependencies**
- Internal: `common-core`, `common-security`, `common-kafka`, `common-observability`, `common-test-support` (test scope).
- External: `spring-boot-starter-webflux`, `spring-boot-starter-data-r2dbc`, `r2dbc-postgresql`, `postgresql`, `flyway-core` + `flyway-database-postgresql`, `spring-boot-starter-data-redis-reactive`, `resilience4j-spring-boot3` + `resilience4j-reactor`, Lombok, `spring-boot-starter-test` + Testcontainers + Reactor Test + WireMock (test scope, for simulating the Banking System's accept/reject/timeout responses).

**5. Public API** — N/A (build file).

**6. Internal Workflow** — Standard Maven module resolution.

**7. Engineering Considerations** — This service's HPA scaling signal (batch-queue depth, per `Deployment-Architecture.md` §5) is an application-level metric, not a build-time concern, so no additional POM dependency is implied by that design choice beyond the already-declared Micrometer support in `common-observability`.

**8. Testing Strategy** — Verified by `mvn clean install`; duplicate-batch-prevention and reconciliation-on-recovery scenarios are verified in `src/it` against Testcontainers Postgres with concurrent batch-creation simulation.

**9. Future Extension** — A future real-time/instant-settlement capability (per this service's own Future Enhancements section) would not require new build dependencies, only new scheduling configuration.

---

### api-gateway/pom.xml
**Relative Path:** `api-gateway/pom.xml`

**1. File Overview** — Module: api-gateway | Layer: Build configuration | Doc Cross-Reference: `API-Gateway-Part-01.md` §9–12

**2. Purpose** — Declares the API Gateway as a reactive Spring Cloud Gateway-based module, the platform's single external ingress point and the last service wired up in the build order (since it routes to every other service).

**3. Responsibilities**
- Depends on `common-core`, `common-security`, `common-observability` (production) and `common-test-support` (test) — deliberately does **not** depend on `common-kafka`, since the Gateway has no Kafka producer/consumer role at all per its own architecture spec.
- Declares Spring Cloud Gateway (reactive), Redis reactive (rate-limit/circuit-breaker state), Resilience4j, and JWT/OAuth2 validation support (already substantially covered by `common-security`, with the Gateway itself declaring `spring-cloud-starter-gateway` as its distinguishing dependency).
- Must NOT declare a dependency on any service module — the Gateway's routing table is configuration-driven (service discovery), not compile-time-coupled to any service's code.

**4. Dependencies**
- Internal: `common-core`, `common-security`, `common-observability`, `common-test-support` (test scope). Explicitly **not** `common-kafka`.
- External: `spring-cloud-starter-gateway` (or `spring-cloud-starter-gateway-server-webflux` depending on the Spring Cloud release train's exact artifact naming at implementation time), `spring-boot-starter-data-redis-reactive`, `resilience4j-spring-boot3` + `resilience4j-reactor`, Lombok, `spring-boot-starter-test` + Reactor Test + WireMock (test scope, for simulating downstream service responses in routing tests).

**5. Public API** — N/A (build file).

**6. Internal Workflow** — Standard Maven module resolution.

**7. Engineering Considerations** — The deliberate absence of `common-kafka` here is itself an architectural assertion worth preserving in code review: if a future change ever adds a Kafka dependency to this POM, that should trigger a design conversation, since it would mean the Gateway has grown a messaging responsibility it was explicitly scoped never to have.

**8. Testing Strategy** — Verified by `mvn clean install`; routing/rate-limit/circuit-breaker integration tests run against WireMock-simulated downstream services, since this module has no database of its own to Testcontainers-back.

**9. Future Extension** — A future Spring Cloud version upgrade is isolated to this POM's Gateway starter version, managed centrally via the root POM's `dependencyManagement` for the Spring Cloud BOM.

---

### provider-simulator/pom.xml
**Relative Path:** `provider-simulator/pom.xml`

**1. File Overview** — Module: provider-simulator | Layer: Build configuration | Doc Cross-Reference: `Acquiring-Adapter-Part-01.md` §5 (External Acquirers, simulated)

**2. Purpose** — Declares the single, profile-driven provider/bank simulator module (consolidated from what would otherwise be five near-duplicate modules per the platform's re-engineering pass) used by Acquiring Adapter's connectors and the Net Banking flow in non-production environments only.

**3. Responsibilities**
- Depends only on a minimal Spring Boot web starter set — deliberately does **not** depend on any `platform/*` shared library, since this is sandbox/test tooling, not part of the production system boundary, and should not inherit the platform's production-grade cross-cutting concerns (mTLS enforcement, Outbox/Inbox, etc.) that would be meaningless for a scenario-driven stub.
- Declares Spring Boot Web (Servlet, simplest sufficient stack for a stub responder) and Spring profile support to switch between `providera`/`providerb`/`providerc`/`providerd`/`netbanking` behavior via `application-<profile>.yml`.
- Must NOT be included in any service's compile-time dependency graph — it is reached only over HTTP at runtime, exactly as a real external acquirer would be.

**4. Dependencies**
- Internal: none.
- External: `spring-boot-starter-web`, `spring-boot-starter-test` (test scope).

**5. Public API** — N/A (build file).

**6. Internal Workflow** — Standard Maven module resolution; deployed as five separate running instances (one per active Spring profile) despite being one build artifact.

**7. Engineering Considerations** — Keeping this module dependency-free from `platform/*` is a deliberate re-engineering decision: a production-grade shared library dependency here would blur the line between "real system component" and "test double," which this module must never be mistaken for.

**8. Testing Strategy** — Minimal self-tests only (does the stub respond per its configured scenario); its real verification happens indirectly, through Acquiring Adapter's own mock-provider integration tests calling it.

**9. Future Extension** — A sixth simulated provider is a new `application-<profile>.yml` and scenario set, never a new Maven module.