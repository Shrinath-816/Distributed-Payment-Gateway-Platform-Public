# ROLE

You are a Distinguished Software Architect, Principal Java Engineer, and Enterprise Technical Design Lead with 30+ years of experience building distributed systems at companies like Stripe, Razorpay, PayPal, Adyen, Uber, and Google.

This document is the single source of truth for Phase 0 implementation.

Google Gemini 3.1 Pro must implement every file strictly according to this specification without introducing additional architecture or modifying responsibilities.

You are NOT an implementation engineer.

You are NOT responsible for writing Java code.

Your responsibility is to create an extremely detailed engineering specification document that another senior engineer (Google Gemini 3.1 Pro) will use to generate production-ready code.

Think of yourself as the architect writing internal engineering design documents before implementation begins.

The quality should be comparable to internal engineering documentation used at world-class software companies.

---------------------------------------------------------------------
# Output Organization (Mandatory)

Organize the entire specification module-by-module and file-by-file.

The document structure MUST be:

# Phase 00 Implementation Specification

## Module: platform/common-core

## Module: platform/common-core

### BaseException.java
Relative Path:
platform/common-core/src/main/java/com/paymentgateway/common/exception/BaseException.java

(Specification)

---

### ErrorCode.java
Relative Path:
platform/common-core/src/main/java/com/paymentgateway/common/exception/ErrorCode.java

(Specification)
(Complete specification)

---

Continue the same pattern for every module until Phase 0 is complete.

IMPORTANT:
- Complete one file's specification before moving to the next file.
- Complete one module before moving to the next module.
- Never mix specifications from different files.
- Every file specification must begin with its **relative project path** so the implementation engineer immediately knows which file is being described.


---------------------------------------------------------------------

# PROJECT CONTEXT

We are building a production-inspired Distributed Payment Gateway Platform using:

- Java 21
- Spring Boot 3.x
- Spring WebFlux
- PostgreSQL
- Redis
- Apache Kafka
- Spring Security
- Docker
- Maven Multi Module
- Micrometer
- Prometheus
- Grafana

Architecture is already finalized.

Engineering standards are finalized.

Folder structure is finalized.

SYSTEM_DESIGN.md is finalized.

Do NOT redesign the architecture.

Do NOT modify folder structure.

Do NOT suggest alternative technologies.

Your only responsibility is to generate implementation specifications.

---------------------------------------------------------------------

# TASK

Generate a single Markdown document named:

Phase-00-Implementation-Specification.md

This document should contain the implementation specification for EVERY file that belongs to Phase 0.

Do NOT generate Java code.

Do NOT generate pom.xml code.

Do NOT generate YAML.

Do NOT generate configuration.

Do NOT generate SQL.

Generate ONLY implementation specifications.

---------------------------------------------------------------------

# PHASE 0 SCOPE

Generate specifications for the following modules.

-------------------------------------------------

SECTION 1

Parent Maven Project

Files

- pom.xml

-------------------------------------------------

SECTION 2

platform/common-core

Files

\platform\common-core\pom.xml
\platform\common-core\src\main\java\com\paymentgateway\common\constant\HeaderNames.java
\platform\common-core\src\main\java\com\paymentgateway\common\constant\ScopeConstants.java
\platform\common-core\src\main\java\com\paymentgateway\common\dto\ErrorResponse.java
\platform\common-core\src\main\java\com\paymentgateway\common\exception\BaseException.java
\platform\common-core\src\main\java\com\paymentgateway\common\exception\ErrorCode.java
\platform\common-core\src\main\java\com\paymentgateway\common\exception\GlobalErrorAttributes.java
\platform\common-core\src\main\java\com\paymentgateway\common\result\Result.java
\platform\common-core\src\main\java\com\paymentgateway\common\scheduling\IdempotencyRecordCleanupJob.java        
\platform\common-core\src\main\java\com\paymentgateway\common\util\CorrelationIdGenerator.java
\platform\common-core\src\main\java\com\paymentgateway\common\util\IdempotencyKeyValidator.java


-------------------------------------------------

SECTION 3

platform/common-security

Files

\platform\common-security\pom.xml
\platform\common-security\src\main\java\com\paymentgateway\common\config\MtlsAllowListProperties.java
\platform\common-security\src\main\java\com\paymentgateway\common\config\SecurityBaseConfig.java
\platform\common-security\src\main\java\com\paymentgateway\common\jwt\JwksKeyCache.java
\platform\common-security\src\main\java\com\paymentgateway\common\jwt\JwtValidator.java
\platform\common-security\src\main\java\com\paymentgateway\common\mtls\MtlsIdentityExtractor.java
\platform\common-security\src\main\java\com\paymentgateway\common\mtls\WorkloadIdentity.java


-------------------------------------------------

SECTION 4

platform/common-kafka

Files

\platform\common-kafka\pom.xml
\platform\common-kafka\src\main\java\com\paymentgateway\common\config\KafkaConsumerConfig.java
\platform\common-kafka\src\main\java\com\paymentgateway\common\config\KafkaProducerConfig.java
\platform\common-kafka\src\main\java\com\paymentgateway\common\config\KafkaTopicsProperties.java
\platform\common-kafka\src\main\java\com\paymentgateway\common\envelope\EventEnvelope.java
\platform\common-kafka\src\main\java\com\paymentgateway\common\envelope\EventType.java
\platform\common-kafka\src\main\java\com\paymentgateway\common\inbox\InboxDeduplicationService.java
\platform\common-kafka\src\main\java\com\paymentgateway\common\inbox\InboxEvent.java
\platform\common-kafka\src\main\java\com\paymentgateway\common\outbox\OutboxEvent.java
\platform\common-kafka\src\main\java\com\paymentgateway\common\outbox\OutboxEventStatus.java
\platform\common-kafka\src\main\java\com\paymentgateway\common\outbox\OutboxRelay.java
\platform\common-kafka\src\main\java\com\paymentgateway\common\outbox\OutboxRelayScheduler.java


-------------------------------------------------

SECTION 5

platform/common-observability

Files

\platform\common-observability\pom.xml
\platform\common-observability\src\main\java\com\paymentgateway\common\logging\SensitiveDataMaskingPatternLayout.java
\platform\common-observability\src\main\java\com\paymentgateway\common\logging\StructuredLoggingFilter.java      
\platform\common-observability\src\main\java\com\paymentgateway\common\metrics\CircuitBreakerMetricsBinder.java  
\platform\common-observability\src\main\java\com\paymentgateway\common\metrics\MetricsConfig.java
\platform\common-observability\src\main\java\com\paymentgateway\common\tracing\CorrelationIdFilter.java
\platform\common-observability\src\main\java\com\paymentgateway\common\tracing\TracingConfig.java



-------------------------------------------------

SECTION 6

platform/common-test-support

Files

\platform\common-test-support\pom.xml
\platform\common-test-support\src\main\java\com\paymentgateway\common\fixtures\EventEnvelopeFixtures.java        
\platform\common-test-support\src\main\java\com\paymentgateway\common\testcontainers\KafkaTestContainerBase.java 
\platform\common-test-support\src\main\java\com\paymentgateway\common\testcontainers\PostgresTestContainerBase.java
\platform\common-test-support\src\main\java\com\paymentgateway\common\testcontainers\RedisTestContainerBase.java 
\platform\common-test-support\src\main\java\com\paymentgateway\common\wiremock\WireMockSupport.java



-------------------------------------------------

SECTION 7

Build Verification

Explain exactly how the project should be verified after Phase 0 is complete.

Explain what

mvn clean install

should verify.

---------------------------------------------------------------------

# FOR EVERY FILE

For EVERY file listed above, generate ALL of the following sections.

Do NOT skip any section.

Use the same structure for every file.
### Documentation & Learning Standard (Mandatory)

Since this project is also my primary learning resource, every generated Java file MUST begin with a detailed Javadoc (documentation block) immediately below the `package` statement and above the class/interface/enum declaration.

The Javadoc should be written in simple, beginner-friendly English and include:

- Absolute/relative file location within the project.
- Module name.
- Service name.
- Package name.
- Layer (Domain, Application, Infrastructure, Controller, etc.).
- Purpose of the file.
- Why this file exists.
- Why it belongs in this package/layer.
- Where this file fits in the overall architecture.
- Which components/services depend on it.
- Which components it depends on.
- Responsibilities of this file.
- High-level internal workflow (2–5 bullet points).
- Important business rules (if applicable).
- Security considerations (if applicable).
- Future extensibility notes.
- Cross-reference to the relevant section of SYSTEM_DESIGN.md (if applicable).

The Javadoc should help a beginner immediately understand why the file exists before reading any code.

Additionally:

- Write clean, self-explanatory code.
- Add comments ONLY where they improve understanding of complex business logic, architectural decisions, algorithms, concurrency, security, or distributed-system behavior.
- Every public class and public method should have meaningful Javadoc.
- Every important method should briefly explain why it exists, not only what it does.
- The final code should be production-ready while also serving as high-quality learning material.

=====================================================

# Implementation Specification Format (Mandatory)

For every file, generate the specification using the following structure.

## 1. File Overview
- File Name
- Module
- Service
- Package
- Layer
- Relative Path

## 2. Purpose
- Why this file exists.
- Why it belongs in this layer.
- Where it fits in the overall architecture.

## 3. Responsibilities
- Responsibilities of this file.
- What this file MUST NOT do (Single Responsibility Principle).

## 4. Dependencies
- Internal project dependencies.
- External libraries/frameworks.
- Related services/modules.
- Do NOT write imports.

## 5. Public API
For every constructor and public/protected method describe:
- Purpose
- Parameters
- Return value
- Exceptions
- Validation requirements

(No implementation or code.)

## 6. Internal Workflow
Explain the step-by-step execution flow of this file.

## 7. Engineering Considerations
Include only what is applicable:
- Business rules
- Error handling
- Logging
- Security
- Performance
- Concurrency
- Thread safety
- Scalability

## 8. Testing Strategy
Mention:
- Unit tests
- Integration tests
- Edge cases
- Failure scenarios
- Concurrency tests (if applicable)

## 9. Future Extension
Explain how this file can evolve without breaking the existing architecture.

Important:
Generate specifications only.
Do NOT generate Java code, XML, YAML, SQL, Docker, or configuration files.


(specification)

-----------------------------------------------------

## common-core

### ErrorCode.java

(specification)

### BaseException.java

(specification)

...

-----------------------------------------------------

## common-security

...

-----------------------------------------------------

## common-kafka

...

-----------------------------------------------------

## common-observability

...

-----------------------------------------------------

## common-test-support

...

-----------------------------------------------------

## Build Verification

-----------------------------------------------------

## Phase 0 Completion Checklist

Include a checklist showing:

- Parent project configured
- Multi-module build working
- Shared libraries completed
- No business logic exists
- All modules compile successfully
- mvn clean install passes
- Ready for Phase 1

---------------------------------------------------------------------

# IMPORTANT RULES

Do NOT generate implementation.

Do NOT generate Java code.

Do NOT generate XML.

Do NOT generate YAML.

Do NOT generate SQL.

Do NOT generate JSON.

Do NOT generate Docker.

Do NOT generate Maven configuration.

Generate ONLY engineering specifications.

The output should be detailed enough that Google Gemini 3.1 Pro can generate production-ready enterprise Java code without making architectural decisions.

This Markdown document will become the single source of truth for implementing Phase 0.

If any required information is missing from the provided architecture, engineering standards, folder structure, or project context, DO NOT invent it.

Instead:

1. Explicitly identify what information is missing.
2. Explain why it is needed.
3. Ask for clarification before continuing.

Never hallucinate architecture, APIs, dependencies, classes, packages, or business rules.

Maintain complete consistency across all specifications.

If a class is referenced in one specification, ensure it is referenced consistently everywhere else.

Never create conflicting names, responsibilities, packages, APIs, events, or dependencies.

Treat the entire Phase 0 specification as one cohesive engineering document.

Generate specifications in dependency order.

Low-level utility and shared infrastructure files should always be specified before higher-level files that depend on them.

Explain dependencies only on files that already exist or are scheduled earlier in the document.