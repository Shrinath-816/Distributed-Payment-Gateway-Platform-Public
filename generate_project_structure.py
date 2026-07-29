import os

ROOT = "distributed-payment-gateway"
files = []

# ---------------------------------------------------------------------------
# ROOT LEVEL (unchanged)
# ---------------------------------------------------------------------------
files += [
    "pom.xml", "README.md", "docker-compose.yml", ".gitignore",
    ".env.example", ".editorconfig",
]

# ---------------------------------------------------------------------------
# DOCS (unchanged - source of truth, never touched)
# ---------------------------------------------------------------------------
docs_root_files = ["01_PROJECT_CONTEXT.md", "02_ENGINEERING_STANDARDS.md", "SYSTEM_DESIGN.md"]
for svc in ["API-Gateway", "Merchant-Service", "Token-Vault", "Payment-Orchestrator",
            "Acquiring-Adapter", "Webhook-Service", "Settlement-Service"]:
    for p in range(1, 5):
        docs_root_files.append(f"{svc}-Part-{p:02d}.md")
docs_root_files.append("Browser-SDK.md")
for f in docs_root_files:
    files.append(f"docs/{f}")
for f in ["Event-Catalog.md", "Database-Architecture.md", "Security-Architecture.md",
          "Deployment-Architecture.md", "Observability.md", "API-Standards.md",
          "Coding-Guidelines.md", "Sequence-Diagrams.md"]:
    files.append(f"docs/cross-cutting/{f}")
files.append("docs/adr/.gitkeep")
for f in ["ADR-0001-database-per-service.md", "ADR-0002-no-2pc-saga.md",
          "ADR-0003-jdbc-vs-r2dbc.md", "ADR-0004-outbox-relay-implementation.md",
          "ADR-0005-token-vault-dual-listener.md",
          "ADR-0006-reengineering-boilerplate-reduction.md"]:
    files.append(f"docs/adr/{f}")

# ---------------------------------------------------------------------------
# PLATFORM (shared libraries) — EXPANDED to absorb genuinely shared logic
# ---------------------------------------------------------------------------
def platform_module(name, java_files, resource_files=None):
    base = f"platform/{name}"
    files.append(f"{base}/pom.xml")
    pkg = f"{base}/src/main/java/com/paymentgateway/common"
    for jf in java_files:
        files.append(f"{pkg}/{jf}")
    for rf in (resource_files or []):
        files.append(f"{base}/src/main/resources/{rf}")
    files.append(f"{base}/src/test/java/com/paymentgateway/common/.gitkeep")

platform_module("common-core", [
    "exception/BaseException.java",
    "exception/ErrorCode.java",
    "exception/GlobalErrorAttributes.java",
    "result/Result.java",
    "constant/HeaderNames.java",
    "constant/ScopeConstants.java",
    "util/CorrelationIdGenerator.java",
    "util/IdempotencyKeyValidator.java",
    "dto/ErrorResponse.java",                          # NEW: single shared error DTO, replaces per-service duplicates
    "scheduling/IdempotencyRecordCleanupJob.java",      # NEW: single reusable cleanup job, replaces 3 per-service copies
])

platform_module("common-security", [
    "mtls/MtlsIdentityExtractor.java",
    "mtls/WorkloadIdentity.java",
    "jwt/JwtValidator.java",
    "jwt/JwksKeyCache.java",
    "config/SecurityBaseConfig.java",
    "config/MtlsAllowListProperties.java",
])

platform_module("common-kafka", [
    "envelope/EventEnvelope.java",
    "envelope/EventType.java",
    "outbox/OutboxEvent.java",
    "outbox/OutboxEventStatus.java",
    "outbox/OutboxRelay.java",
    "outbox/OutboxRelayScheduler.java",
    "inbox/InboxEvent.java",
    "inbox/InboxDeduplicationService.java",
    "config/KafkaProducerConfig.java",
    "config/KafkaConsumerConfig.java",
    "config/KafkaTopicsProperties.java",
])

platform_module("common-observability", [
    "tracing/TracingConfig.java",
    "tracing/CorrelationIdFilter.java",
    "metrics/MetricsConfig.java",
    "metrics/CircuitBreakerMetricsBinder.java",
    "logging/StructuredLoggingFilter.java",
    "logging/SensitiveDataMaskingPatternLayout.java",
])

platform_module("common-test-support", [
    "testcontainers/PostgresTestContainerBase.java",
    "testcontainers/KafkaTestContainerBase.java",
    "testcontainers/RedisTestContainerBase.java",
    "wiremock/WireMockSupport.java",
    "fixtures/EventEnvelopeFixtures.java",
])

# ---------------------------------------------------------------------------
# HELPERS (identical to original scaffold generator)
# ---------------------------------------------------------------------------
def add(base, subpath, filelist):
    for f in filelist:
        files.append(f"{base}/{subpath}/{f}")

def std_resources(base, service_name, migrations):
    r = f"{base}/src/main/resources"
    files.append(f"{r}/application.yml")
    files.append(f"{r}/application-dev.yml")
    files.append(f"{r}/application-staging.yml")
    files.append(f"{r}/application-uat.yml")
    files.append(f"{r}/application-production.yml")
    files.append(f"{r}/logback-spring.xml")
    files.append(f"{r}/db/migration/.gitkeep")
    for i, m in enumerate(migrations, start=1):
        files.append(f"{r}/db/migration/V{i}__{m}.sql")

def std_docker_k8s(base, service_name, extra_k8s=None):
    files.append(f"{base}/Dockerfile")
    files.append(f"{base}/.dockerignore")
    k8s = f"infra/kubernetes/base/{service_name}"
    for f in ["deployment.yaml", "service.yaml", "configmap.yaml",
              "hpa.yaml", "poddisruptionbudget.yaml", "kustomization.yaml"]:
        files.append(f"{k8s}/{f}")
    for f in (extra_k8s or []):
        files.append(f"{k8s}/{f}")

def std_test_mirror(base, main_classes_pkgpath, unit_tests, integration_tests):
    t = f"{base}/src/test/java/com/paymentgateway"
    for f in unit_tests:
        files.append(f"{t}/{main_classes_pkgpath}/{f}")
    it = f"{base}/src/it/java/com/paymentgateway"
    for f in integration_tests:
        files.append(f"{it}/{main_classes_pkgpath}/{f}")
    files.append(f"{base}/src/test/resources/application-test.yml")

# ---------------------------------------------------------------------------
# 1. API GATEWAY  — merge: CorsConfig -> SecurityConfig; remove local HeaderNames dup
# ---------------------------------------------------------------------------
base = "api-gateway"
files.append(f"{base}/pom.xml")
pkg = f"{base}/src/main/java/com/paymentgateway/gateway"

add(base, "src/main/java/com/paymentgateway/gateway/config", [
    "GatewayRouteConfig.java", "SecurityConfig.java", "RateLimitConfig.java", "ResilienceConfig.java",
])
add(base, "src/main/java/com/paymentgateway/gateway/domain/route", ["RouteClass.java", "RouteDefinition.java"])
add(base, "src/main/java/com/paymentgateway/gateway/domain/policy", ["ScopePolicy.java", "RateLimitPolicy.java"])
add(base, "src/main/java/com/paymentgateway/gateway/domain/principal", ["AuthenticatedPrincipal.java"])
add(base, "src/main/java/com/paymentgateway/gateway/application", [
    "AuthenticateRequestUseCase.java", "AuthorizeRequestUseCase.java",
    "ResolveRouteUseCase.java", "ApplyRateLimitUseCase.java",
])
add(base, "src/main/java/com/paymentgateway/gateway/port", [
    "TokenValidationPort.java", "RateLimitStorePort.java",
    "ServiceDiscoveryPort.java", "TracingContextPort.java",
])
add(base, "src/main/java/com/paymentgateway/gateway/adapter/security", [
    "JwtTokenValidationAdapter.java", "OAuth2TokenValidationAdapter.java", "ApiKeyValidationAdapter.java",
])
add(base, "src/main/java/com/paymentgateway/gateway/adapter/ratelimit", ["RedisRateLimitStoreAdapter.java"])
add(base, "src/main/java/com/paymentgateway/gateway/adapter/discovery", ["KubernetesServiceDiscoveryAdapter.java"])
add(base, "src/main/java/com/paymentgateway/gateway/adapter/tracing", ["OtelTracingContextAdapter.java"])
add(base, "src/main/java/com/paymentgateway/gateway/filter", [
    "CorrelationTraceFilter.java", "StructuralValidationFilter.java",
    "AuthenticationFilter.java", "CoarseAuthorizationFilter.java",
    "RateLimitFilter.java", "IdempotencyHeaderFilter.java", "ResponseMappingFilter.java",
])
add(base, "src/main/java/com/paymentgateway/gateway/error", ["GatewayErrorModel.java", "GlobalErrorHandler.java"])
add(base, "src/main/java/com/paymentgateway/gateway/health", ["LivenessIndicator.java", "ReadinessIndicator.java"])
files.append(f"{pkg}/ApiGatewayApplication.java")

std_resources(base, "api-gateway", ["init_gateway_route_table"])
std_docker_k8s(base, "api-gateway", extra_k8s=["ingress.yaml", "networkpolicy.yaml"])
std_test_mirror(base, "gateway",
    unit_tests=["filter/AuthenticationFilterTest.java", "filter/RateLimitFilterTest.java",
                "domain/policy/ScopePolicyTest.java"],
    integration_tests=["GatewayRoutingIntegrationTest.java", "RateLimitIntegrationTest.java"])

# ---------------------------------------------------------------------------
# 2. MERCHANT SERVICE — merge: events->1, vo->1, exceptions->2, dto->2, constants->1;
#                        remove: event/producer (folded into outbox adapter)
# ---------------------------------------------------------------------------
base = "merchant-service"
files.append(f"{base}/pom.xml")
pkg = f"{base}/src/main/java/com/paymentgateway/merchant"

add(base, "src/main/java/com/paymentgateway/merchant/config", [
    "SecurityConfig.java", "KafkaConfig.java", "RedisConfig.java", "OpenApiConfig.java",
])
add(base, "src/main/java/com/paymentgateway/merchant/controller", [
    "MerchantController.java", "CredentialController.java", "KycController.java",
    "WebhookConfigController.java", "PayoutAccountController.java",
    "PaymentMethodController.java", "InternalAuthViewController.java",
])
add(base, "src/main/java/com/paymentgateway/merchant/application/command", [
    "RegisterMerchantUseCase.java", "SuspendMerchantUseCase.java", "DeactivateMerchantUseCase.java",
    "IssueCredentialUseCase.java", "RevokeCredentialUseCase.java", "RotateCredentialUseCase.java",
    "SubmitKycDocumentsUseCase.java", "RecordVerificationDecisionUseCase.java",
    "ConfigureWebhookUseCase.java", "ConfigurePayoutAccountUseCase.java", "ConfigurePaymentMethodsUseCase.java",
])
add(base, "src/main/java/com/paymentgateway/merchant/application/query", [
    "GetMerchantAuthViewUseCase.java", "GetMerchantProfileUseCase.java",
    "GetKycCaseUseCase.java", "ListCredentialsUseCase.java", "ListWebhookConfigsUseCase.java",
])
add(base, "src/main/java/com/paymentgateway/merchant/domain/merchant", [
    "Merchant.java", "MerchantLifecycleState.java", "WebhookConfig.java",
    "PayoutAccount.java", "PaymentMethodConfig.java",
])
add(base, "src/main/java/com/paymentgateway/merchant/domain/credential", [
    "Credential.java", "CredentialType.java", "CredentialStatus.java", "CredentialIssuancePolicy.java",
])
add(base, "src/main/java/com/paymentgateway/merchant/domain/kyc", [
    "KycCase.java", "KycCaseStatus.java", "DocumentReference.java", "VerificationDecision.java",
])
# MERGED: 10 trivial event records -> 1 file
add(base, "src/main/java/com/paymentgateway/merchant/domain/event", ["MerchantDomainEvents.java"])
# MERGED: 7 trivial value objects -> 1 file
add(base, "src/main/java/com/paymentgateway/merchant/domain/vo", ["MerchantValueObjects.java"])
add(base, "src/main/java/com/paymentgateway/merchant/port", [
    "MerchantRepositoryPort.java", "CredentialRepositoryPort.java", "KycCaseRepositoryPort.java",
    "OutboxWriterPort.java", "DocumentStoreClientPort.java", "EncryptionPort.java",
    "MerchantAuthViewRepositoryPort.java",
])
add(base, "src/main/java/com/paymentgateway/merchant/adapter/persistence", [
    "MerchantJpaRepositoryAdapter.java", "CredentialJpaRepositoryAdapter.java",
    "KycCaseJpaRepositoryAdapter.java", "MerchantAuthViewRepositoryAdapter.java",
])
# OutboxWriterAdapter now also builds the EventEnvelope (event/producer folded in)
add(base, "src/main/java/com/paymentgateway/merchant/adapter/outbox", ["OutboxWriterAdapter.java"])
add(base, "src/main/java/com/paymentgateway/merchant/adapter/documentstore", ["DocumentStoreClientAdapter.java"])
add(base, "src/main/java/com/paymentgateway/merchant/adapter/encryption", ["Aes256EncryptionAdapter.java"])
add(base, "src/main/java/com/paymentgateway/merchant/entity", [
    "MerchantEntity.java", "CredentialEntity.java", "KycCaseEntity.java",
    "DocumentReferenceEntity.java", "VerificationDecisionEntity.java",
    "WebhookConfigEntity.java", "PayoutAccountEntity.java",
    "MerchantLifecycleAuditEntity.java", "MerchantAuthViewEntity.java",
    "OutboxEventEntity.java", "IdempotencyRecordEntity.java",
])
add(base, "src/main/java/com/paymentgateway/merchant/repository", [
    "MerchantJpaRepository.java", "CredentialJpaRepository.java", "KycCaseJpaRepository.java",
    "MerchantAuthViewJpaRepository.java", "OutboxEventJpaRepository.java", "IdempotencyRecordJpaRepository.java",
])
# MERGED: 9 request DTOs -> 1 file; 8 response DTOs -> 1 file (ErrorResponse moved to common-core)
add(base, "src/main/java/com/paymentgateway/merchant/dto/request", ["MerchantRequests.java"])
add(base, "src/main/java/com/paymentgateway/merchant/dto/response", ["MerchantResponses.java"])
add(base, "src/main/java/com/paymentgateway/merchant/mapper", [
    "MerchantMapper.java", "CredentialMapper.java", "KycCaseMapper.java",
    "WebhookConfigMapper.java", "PayoutAccountMapper.java",
])
# MERGED: 8 trivial exception classes -> 1 file; handler stays separate (real @ExceptionHandler logic)
add(base, "src/main/java/com/paymentgateway/merchant/exception", [
    "MerchantExceptions.java", "GlobalExceptionHandler.java",
])
add(base, "src/main/java/com/paymentgateway/merchant/security", ["InternalServiceAuthFilter.java", "MtlsIdentityValidator.java"])
add(base, "src/main/java/com/paymentgateway/merchant/validation", [
    "WebhookUrlValidator.java", "TaxIdentifierValidator.java", "ScopeValidator.java",
])
add(base, "src/main/java/com/paymentgateway/merchant/event/consumer", ["MerchantSelfConsumer.java"])
add(base, "src/main/java/com/paymentgateway/merchant/scheduler", ["CredentialRotationGraceWindowScheduler.java"])
# MERGED: MerchantConstants + ScopeConstants -> 1 file
add(base, "src/main/java/com/paymentgateway/merchant/constant", ["MerchantConstants.java"])
files.append(f"{pkg}/MerchantServiceApplication.java")

std_resources(base, "merchant-service", [
    "create_merchant_table", "create_credential_table", "create_kyc_case_table",
    "create_document_reference_table", "create_verification_decision_table",
    "create_webhook_config_table", "create_payout_account_table",
    "create_merchant_lifecycle_audit_table", "create_merchant_auth_view_table",
    "create_outbox_event_table", "create_idempotency_record_table",
])
std_docker_k8s(base, "merchant-service")
std_test_mirror(base, "merchant",
    unit_tests=["domain/merchant/MerchantTest.java",
                "domain/credential/CredentialIssuancePolicyTest.java",
                "application/command/RegisterMerchantUseCaseTest.java"],
    integration_tests=["MerchantRegistrationIntegrationTest.java",
                        "CredentialIssuanceIntegrationTest.java",
                        "MerchantAuthViewProjectionIntegrationTest.java"])

# ---------------------------------------------------------------------------
# 3. TOKEN VAULT SERVICE — merge: events->1, vo->1, exceptions->2, dto->2,
#                           config(4->2), scheduler(3->1)
# ---------------------------------------------------------------------------
base = "token-vault-service"
files.append(f"{base}/pom.xml")
pkg = f"{base}/src/main/java/com/paymentgateway/vault"

# MERGED: MtlsAllowListConfig -> SecurityConfig ; KmsClientConfig -> EncryptionConfig
add(base, "src/main/java/com/paymentgateway/vault/config", ["SecurityConfig.java", "EncryptionConfig.java"])
add(base, "src/main/java/com/paymentgateway/vault/controller/public_", [
    "TokenizationController.java", "TokenMetadataController.java",
])
add(base, "src/main/java/com/paymentgateway/vault/controller/internal", [
    "DetokenizationController.java", "TokenLifecycleController.java", "KeyManagementController.java",
])
add(base, "src/main/java/com/paymentgateway/vault/application", [
    "TokenizePanUseCase.java", "DetokenizeTokenUseCase.java", "RotateTokenUseCase.java",
    "RevokeTokenUseCase.java", "InitiateKeyRotationUseCase.java",
])
add(base, "src/main/java/com/paymentgateway/vault/domain/token", ["Token.java", "TokenStatus.java", "VaultTokenFactory.java"])
add(base, "src/main/java/com/paymentgateway/vault/domain/key", ["KeyMaterial.java", "KeyStatus.java"])
add(base, "src/main/java/com/paymentgateway/vault/domain/service", [
    "TokenizationService.java", "DetokenizationService.java", "KeyRotationService.java",
])
add(base, "src/main/java/com/paymentgateway/vault/domain/specification", [
    "TokenIsDetokenizableSpecification.java", "KeyVersionIsActiveSpecification.java",
])
# MERGED: 6 events -> 1
add(base, "src/main/java/com/paymentgateway/vault/domain/event", ["VaultDomainEvents.java"])
# MERGED: 4 value objects -> 1
add(base, "src/main/java/com/paymentgateway/vault/domain/vo", ["VaultValueObjects.java"])
add(base, "src/main/java/com/paymentgateway/vault/port", [
    "KeyWrappingPort.java", "TokenRepositoryPort.java", "KeyMaterialRepositoryPort.java",
    "AuditWriterPort.java", "OutboxWriterPort.java",
])
add(base, "src/main/java/com/paymentgateway/vault/adapter/hsmkms", ["HsmKmsClientAdapter.java"])
add(base, "src/main/java/com/paymentgateway/vault/adapter/persistence", [
    "TokenRepositoryAdapter.java", "KeyMaterialRepositoryAdapter.java",
])
add(base, "src/main/java/com/paymentgateway/vault/adapter/audit", ["AuditWriterAdapter.java"])
add(base, "src/main/java/com/paymentgateway/vault/adapter/cache", ["RedisTokenCacheAdapter.java"])
add(base, "src/main/java/com/paymentgateway/vault/adapter/outbox", ["OutboxWriterAdapter.java"])
add(base, "src/main/java/com/paymentgateway/vault/entity", [
    "TokenEntity.java", "KeyVersionMetadataEntity.java",
    "OutboxEventEntity.java", "IdempotencyRecordEntity.java", "AuditEntryEntity.java",
])
add(base, "src/main/java/com/paymentgateway/vault/repository", [
    "TokenJpaRepository.java", "KeyVersionMetadataJpaRepository.java",
    "OutboxEventJpaRepository.java", "AuditEntryJpaRepository.java",
])
# MERGED: 3 request DTOs -> 1 ; 4 response DTOs -> 1
add(base, "src/main/java/com/paymentgateway/vault/dto/request", ["VaultRequests.java"])
add(base, "src/main/java/com/paymentgateway/vault/dto/response", ["VaultResponses.java"])
add(base, "src/main/java/com/paymentgateway/vault/mapper", ["TokenMapper.java", "KeyMaterialMapper.java"])
# MERGED: 4 exceptions -> 1
add(base, "src/main/java/com/paymentgateway/vault/exception", ["VaultExceptions.java", "GlobalExceptionHandler.java"])
# NOT merged: two genuinely distinct security filters for the two isolated listeners
add(base, "src/main/java/com/paymentgateway/vault/security", [
    "WorkloadIdentityAllowListFilter.java", "PublicRateLimitFilter.java",
])
add(base, "src/main/java/com/paymentgateway/vault/validation", ["PanValidator.java", "LuhnValidator.java"])
# MERGED: 3 schedulers -> 1
add(base, "src/main/java/com/paymentgateway/vault/scheduler", ["VaultMaintenanceScheduler.java"])
add(base, "src/main/java/com/paymentgateway/vault/constant", ["VaultConstants.java"])
files.append(f"{pkg}/TokenVaultServiceApplication.java")

std_resources(base, "token-vault-service", [
    "create_token_table", "create_key_version_metadata_table",
    "create_outbox_event_table", "create_idempotency_record_table",
])
files.append(f"{base}/src/main/resources/db/migration-audit/.gitkeep")
files.append(f"{base}/src/main/resources/db/migration-audit/V1__create_audit_entry_table.sql")
std_docker_k8s(base, "token-vault-service",
               extra_k8s=["networkpolicy.yaml", "service-public.yaml", "service-internal.yaml"])
std_test_mirror(base, "vault",
    unit_tests=["domain/token/TokenTest.java",
                "domain/specification/TokenIsDetokenizableSpecificationTest.java",
                "domain/service/TokenizationServiceTest.java"],
    integration_tests=["TokenizationIntegrationTest.java", "DetokenizationIntegrationTest.java",
                        "KeyRotationIntegrationTest.java", "UnauthorizedAccessDenialIntegrationTest.java"])

# ---------------------------------------------------------------------------
# 4. PAYMENT ORCHESTRATOR — merge: events->1, vo->1, exceptions->2, dto->2,
#                            scheduler(2->1, idempotency job now shared)
# ---------------------------------------------------------------------------
base = "payment-orchestrator-service"
files.append(f"{base}/pom.xml")
pkg = f"{base}/src/main/java/com/paymentgateway/orchestrator"

add(base, "src/main/java/com/paymentgateway/orchestrator/config", [
    "SecurityConfig.java", "ResilienceConfig.java", "RoutingConfig.java",
])
add(base, "src/main/java/com/paymentgateway/orchestrator/controller", ["PaymentController.java"])
add(base, "src/main/java/com/paymentgateway/orchestrator/application", [
    "CreatePaymentUseCase.java", "AuthorizePaymentUseCase.java", "CapturePaymentUseCase.java",
    "CancelPaymentUseCase.java", "RefundPaymentUseCase.java", "CompensatePaymentUseCase.java",
])
add(base, "src/main/java/com/paymentgateway/orchestrator/domain/payment", [
    "Payment.java", "PaymentState.java", "PaymentRoute.java",
])
add(base, "src/main/java/com/paymentgateway/orchestrator/domain/ledger", ["LedgerEntry.java"])
add(base, "src/main/java/com/paymentgateway/orchestrator/domain/saga", ["SagaExecution.java", "SagaStep.java"])
# MERGED: 8 events -> 1
add(base, "src/main/java/com/paymentgateway/orchestrator/domain/event", ["OrchestratorDomainEvents.java"])
# MERGED: 3 value objects -> 1
add(base, "src/main/java/com/paymentgateway/orchestrator/domain/vo", ["OrchestratorValueObjects.java"])
add(base, "src/main/java/com/paymentgateway/orchestrator/port", [
    "PaymentRepositoryPort.java", "LedgerRepositoryPort.java", "SagaExecutionRepositoryPort.java",
    "OutboxWriterPort.java", "MerchantServiceClientPort.java", "TokenVaultClientPort.java",
    "AcquiringAdapterClientPort.java",
])
add(base, "src/main/java/com/paymentgateway/orchestrator/saga", [
    "SagaCoordinator.java", "RetryManager.java", "CompensationManager.java",
])
add(base, "src/main/java/com/paymentgateway/orchestrator/routing", ["RoutingEngine.java"])
add(base, "src/main/java/com/paymentgateway/orchestrator/adapter/persistence", [
    "PaymentRepositoryAdapter.java", "LedgerRepositoryAdapter.java", "SagaExecutionRepositoryAdapter.java",
])
add(base, "src/main/java/com/paymentgateway/orchestrator/adapter/outbox", ["OutboxWriterAdapter.java"])
add(base, "src/main/java/com/paymentgateway/orchestrator/adapter/client", [
    "MerchantServiceClientAdapter.java", "TokenVaultClientAdapter.java", "AcquiringAdapterClientAdapter.java",
])
add(base, "src/main/java/com/paymentgateway/orchestrator/entity", [
    "PaymentEntity.java", "LedgerEntryEntity.java", "SagaExecutionEntity.java",
    "OutboxEventEntity.java", "IdempotencyRecordEntity.java",
])
# MERGED: 4 request DTOs -> 1 ; 2 response DTOs -> 1
add(base, "src/main/java/com/paymentgateway/orchestrator/dto/request", ["PaymentRequests.java"])
add(base, "src/main/java/com/paymentgateway/orchestrator/dto/response", ["PaymentResponses.java"])
add(base, "src/main/java/com/paymentgateway/orchestrator/mapper", ["PaymentMapper.java", "LedgerEntryMapper.java"])
# MERGED: 7 exceptions -> 1
add(base, "src/main/java/com/paymentgateway/orchestrator/exception", [
    "OrchestratorExceptions.java", "GlobalExceptionHandler.java",
])
add(base, "src/main/java/com/paymentgateway/orchestrator/security", ["InternalServiceAuthFilter.java"])
add(base, "src/main/java/com/paymentgateway/orchestrator/validation", ["PaymentRequestValidator.java"])
add(base, "src/main/java/com/paymentgateway/orchestrator/event/consumer", ["MerchantEventConsumer.java"])
# MERGED: 2 schedulers -> 1 (idempotency cleanup now delegates to platform/common-core shared job)
add(base, "src/main/java/com/paymentgateway/orchestrator/scheduler", ["OrchestratorMaintenanceScheduler.java"])
add(base, "src/main/java/com/paymentgateway/orchestrator/constant", ["OrchestratorConstants.java"])
files.append(f"{pkg}/PaymentOrchestratorApplication.java")

std_resources(base, "payment-orchestrator-service", [
    "create_payment_table", "create_ledger_entry_table", "create_saga_execution_table",
    "create_outbox_event_table", "create_idempotency_record_table",
])
std_docker_k8s(base, "payment-orchestrator-service")
std_test_mirror(base, "orchestrator",
    unit_tests=["domain/payment/PaymentTest.java", "saga/SagaCoordinatorTest.java",
                "routing/RoutingEngineTest.java"],
    integration_tests=["PaymentCreationIntegrationTest.java",
                        "IdempotentDuplicateRequestIntegrationTest.java",
                        "SagaCompensationIntegrationTest.java", "CircuitBreakerChaosTest.java"])

# ---------------------------------------------------------------------------
# 5. ACQUIRING ADAPTER — merge: events->1, vo->1, exceptions->2, dto->2,
#                         connector mapper (req+resp -> 1 per provider),
#                         remove: per-provider client/ (folded into connector),
#                         remove: scheduler (shared common-core job)
# ---------------------------------------------------------------------------
base = "acquiring-adapter-service"
files.append(f"{base}/pom.xml")
pkg = f"{base}/src/main/java/com/paymentgateway/acquiring"

add(base, "src/main/java/com/paymentgateway/acquiring/config", [
    "SecurityConfig.java", "ResilienceConfig.java", "ProviderRegistryConfig.java",
])
add(base, "src/main/java/com/paymentgateway/acquiring/controller", ["AdapterController.java"])
add(base, "src/main/java/com/paymentgateway/acquiring/application", [
    "AuthorizePaymentUseCase.java", "CapturePaymentUseCase.java", "RefundPaymentUseCase.java",
    "VoidPaymentUseCase.java", "GetStatusUseCase.java",
])
add(base, "src/main/java/com/paymentgateway/acquiring/domain/transaction", ["ProviderTransaction.java", "TransactionStatus.java"])
add(base, "src/main/java/com/paymentgateway/acquiring/domain/result", ["AuthorizationResult.java", "Outcome.java"])
# MERGED: 7 events -> 1
add(base, "src/main/java/com/paymentgateway/acquiring/domain/event", ["AcquiringDomainEvents.java"])
# MERGED: 3 value objects -> 1
add(base, "src/main/java/com/paymentgateway/acquiring/domain/vo", ["AcquiringValueObjects.java"])
add(base, "src/main/java/com/paymentgateway/acquiring/port", [
    "ProviderConnector.java", "ProviderTransactionRepositoryPort.java",
    "OutboxWriterPort.java", "ProviderHealthPort.java",
])
add(base, "src/main/java/com/paymentgateway/acquiring/registry", ["AdapterRegistry.java"])
add(base, "src/main/java/com/paymentgateway/acquiring/routing", ["RoutingManager.java"])
add(base, "src/main/java/com/paymentgateway/acquiring/resilience", ["RetryManager.java", "ErrorTranslator.java"])
# MERGED per provider: RequestMapper + ResponseMapper -> one Mapper;
# HTTP client folded into the Connector itself (was a separate client/providerX/*HttpClient.java)
for provider in ["providera", "providerb", "providerc", "providerd"]:
    cap = provider[0].upper() + provider[1:]
    # e.g. providera -> Providera -> but keep original naming ProviderA/B/C/D
    label = "Provider" + provider[-1].upper()
    add(base, f"src/main/java/com/paymentgateway/acquiring/connector/{provider}", [
        f"{label}Connector.java", f"{label}Mapper.java",
    ])
add(base, "src/main/java/com/paymentgateway/acquiring/adapter/persistence", ["ProviderTransactionRepositoryAdapter.java"])
add(base, "src/main/java/com/paymentgateway/acquiring/adapter/outbox", ["OutboxWriterAdapter.java"])
add(base, "src/main/java/com/paymentgateway/acquiring/adapter/health", ["ProviderHealthAdapter.java"])
add(base, "src/main/java/com/paymentgateway/acquiring/entity", [
    "ProviderTransactionEntity.java", "OutboxEventEntity.java", "IdempotencyRecordEntity.java",
])
# MERGED: 4 request DTOs -> 1 ; 2 response DTOs -> 1
add(base, "src/main/java/com/paymentgateway/acquiring/dto/request", ["AuthorizationRequests.java"])
add(base, "src/main/java/com/paymentgateway/acquiring/dto/response", ["AuthorizationResponses.java"])
add(base, "src/main/java/com/paymentgateway/acquiring/mapper", ["ProviderTransactionMapper.java"])
# MERGED: 3 exceptions -> 1
add(base, "src/main/java/com/paymentgateway/acquiring/exception", ["AcquiringExceptions.java", "GlobalExceptionHandler.java"])
add(base, "src/main/java/com/paymentgateway/acquiring/security", ["InternalServiceAuthFilter.java"])
add(base, "src/main/java/com/paymentgateway/acquiring/validation", ["AuthorizationRequestValidator.java"])
add(base, "src/main/java/com/paymentgateway/acquiring/constant", ["AcquiringConstants.java"])
files.append(f"{pkg}/AcquiringAdapterApplication.java")

std_resources(base, "acquiring-adapter-service", [
    "create_provider_transaction_table", "create_outbox_event_table", "create_idempotency_record_table",
])
std_docker_k8s(base, "acquiring-adapter-service")
std_test_mirror(base, "acquiring",
    unit_tests=["connector/providera/ProviderAMapperTest.java",
                "resilience/ErrorTranslatorTest.java", "routing/RoutingManagerTest.java"],
    integration_tests=["AuthorizationIntegrationTest.java", "ProviderFailoverIntegrationTest.java",
                        "AmbiguousTimeoutStatusCheckIntegrationTest.java"])

# ---------------------------------------------------------------------------
# 6. WEBHOOK SERVICE — merge: events->1, vo->1, exceptions->2, dto->1,
#                       consumers(3->1), scheduler(2->1), config(3->2)
# ---------------------------------------------------------------------------
base = "webhook-service"
files.append(f"{base}/pom.xml")
pkg = f"{base}/src/main/java/com/paymentgateway/webhook"

# MERGED: RetryPolicyConfig -> ResilienceConfig
add(base, "src/main/java/com/paymentgateway/webhook/config", ["SecurityConfig.java", "ResilienceConfig.java"])
add(base, "src/main/java/com/paymentgateway/webhook/controller", ["WebhookController.java"])
add(base, "src/main/java/com/paymentgateway/webhook/application", [
    "ProcessEventUseCase.java", "DeliverWebhookUseCase.java",
    "RetryDeliveryUseCase.java", "MoveToDeadLetterUseCase.java",
])
add(base, "src/main/java/com/paymentgateway/webhook/domain/delivery", [
    "Delivery.java", "DeliveryStatus.java", "DeliveryAttempt.java",
])
add(base, "src/main/java/com/paymentgateway/webhook/domain/config", ["WebhookConfigProjection.java"])
# MERGED: 3 events -> 1
add(base, "src/main/java/com/paymentgateway/webhook/domain/event", ["WebhookDomainEvents.java"])
# MERGED: 3 value objects -> 1
add(base, "src/main/java/com/paymentgateway/webhook/domain/vo", ["WebhookValueObjects.java"])
add(base, "src/main/java/com/paymentgateway/webhook/port", [
    "DeliveryRepositoryPort.java", "WebhookConfigProjectionPort.java",
    "OutboxWriterPort.java", "MerchantServiceClientPort.java", "HttpDeliveryClientPort.java",
])
add(base, "src/main/java/com/paymentgateway/webhook/signing", ["SignatureGenerator.java"])
add(base, "src/main/java/com/paymentgateway/webhook/validation", ["EndpointValidator.java"])
add(base, "src/main/java/com/paymentgateway/webhook/retry", ["RetryManager.java", "DlqProcessor.java"])
add(base, "src/main/java/com/paymentgateway/webhook/status", ["DeliveryStatusManager.java"])
add(base, "src/main/java/com/paymentgateway/webhook/adapter/persistence", ["DeliveryRepositoryAdapter.java"])
add(base, "src/main/java/com/paymentgateway/webhook/adapter/outbox", ["OutboxWriterAdapter.java"])
add(base, "src/main/java/com/paymentgateway/webhook/adapter/client", [
    "MerchantServiceClientAdapter.java", "HttpDeliveryClientAdapter.java",
])
add(base, "src/main/java/com/paymentgateway/webhook/adapter/projection", ["WebhookConfigProjectionAdapter.java"])
add(base, "src/main/java/com/paymentgateway/webhook/entity", [
    "DeliveryEntity.java", "DeliveryAttemptEntity.java",
    "WebhookConfigProjectionEntity.java", "OutboxEventEntity.java",
])
# MERGED: request(1) + response(2) -> 1 combined DTO file
add(base, "src/main/java/com/paymentgateway/webhook/dto", ["WebhookDtos.java"])
add(base, "src/main/java/com/paymentgateway/webhook/mapper", ["DeliveryMapper.java"])
# MERGED: 2 exceptions -> 1
add(base, "src/main/java/com/paymentgateway/webhook/exception", ["WebhookExceptions.java", "GlobalExceptionHandler.java"])
add(base, "src/main/java/com/paymentgateway/webhook/security", ["OperatorRoleAuthFilter.java"])
# MERGED: 3 consumers with identical dispatch shape -> 1
add(base, "src/main/java/com/paymentgateway/webhook/event/consumer", ["PlatformEventConsumer.java"])
# MERGED: 2 schedulers -> 1
add(base, "src/main/java/com/paymentgateway/webhook/scheduler", ["WebhookMaintenanceScheduler.java"])
add(base, "src/main/java/com/paymentgateway/webhook/constant", ["WebhookConstants.java"])
files.append(f"{pkg}/WebhookServiceApplication.java")

std_resources(base, "webhook-service", [
    "create_delivery_table", "create_delivery_attempt_table",
    "create_webhook_config_projection_table", "create_outbox_event_table",
])
std_docker_k8s(base, "webhook-service")
std_test_mirror(base, "webhook",
    unit_tests=["signing/SignatureGeneratorTest.java", "retry/RetryManagerTest.java",
                "domain/delivery/DeliveryTest.java"],
    integration_tests=["EventToDeliveryIntegrationTest.java", "DeadLetterQueueIntegrationTest.java",
                        "MockEndpointRetryIntegrationTest.java"])

# ---------------------------------------------------------------------------
# 7. SETTLEMENT SERVICE — merge: events->1, vo->1, exceptions->2, dto->1,
#                          consumers(3->1), scheduler(2->1)
# ---------------------------------------------------------------------------
base = "settlement-service"
files.append(f"{base}/pom.xml")
pkg = f"{base}/src/main/java/com/paymentgateway/settlement"

add(base, "src/main/java/com/paymentgateway/settlement/config", [
    "SecurityConfig.java", "ResilienceConfig.java", "ScheduleConfig.java",
])
add(base, "src/main/java/com/paymentgateway/settlement/controller", ["SettlementController.java"])
add(base, "src/main/java/com/paymentgateway/settlement/application", [
    "ScheduleSettlementUseCase.java", "CreateBatchUseCase.java", "CalculateFeesUseCase.java",
    "GeneratePayoutUseCase.java", "GenerateReconciliationReportUseCase.java",
])
add(base, "src/main/java/com/paymentgateway/settlement/domain/batch", [
    "SettlementBatch.java", "SettlementStatus.java", "SettlementEntry.java",
])
add(base, "src/main/java/com/paymentgateway/settlement/domain/payout", ["Payout.java", "PayoutStatus.java"])
add(base, "src/main/java/com/paymentgateway/settlement/domain/fee", [
    "FeeSchedule.java", "ReservePolicy.java", "FeeCalculationResult.java",
])
# MERGED: 4 events -> 1
add(base, "src/main/java/com/paymentgateway/settlement/domain/event", ["SettlementDomainEvents.java"])
# MERGED: 4 value objects -> 1
add(base, "src/main/java/com/paymentgateway/settlement/domain/vo", ["SettlementValueObjects.java"])
add(base, "src/main/java/com/paymentgateway/settlement/port", [
    "SettlementBatchRepositoryPort.java", "PayoutRepositoryPort.java", "OutboxWriterPort.java",
    "MerchantServiceClientPort.java", "BankingSystemClientPort.java",
])
add(base, "src/main/java/com/paymentgateway/settlement/scheduling", ["ScheduleManager.java"])
add(base, "src/main/java/com/paymentgateway/settlement/batching", ["BatchProcessor.java"])
add(base, "src/main/java/com/paymentgateway/settlement/calculation", ["FeeCalculator.java"])
add(base, "src/main/java/com/paymentgateway/settlement/payout", ["PayoutGenerator.java"])
add(base, "src/main/java/com/paymentgateway/settlement/reconciliation", ["ReconciliationReportBuilder.java"])
add(base, "src/main/java/com/paymentgateway/settlement/status", ["SettlementStatusManager.java"])
add(base, "src/main/java/com/paymentgateway/settlement/adapter/persistence", [
    "SettlementBatchRepositoryAdapter.java", "PayoutRepositoryAdapter.java",
])
add(base, "src/main/java/com/paymentgateway/settlement/adapter/outbox", ["OutboxWriterAdapter.java"])
add(base, "src/main/java/com/paymentgateway/settlement/adapter/client", [
    "MerchantServiceClientAdapter.java", "BankingSystemClientAdapter.java",
])
add(base, "src/main/java/com/paymentgateway/settlement/entity", [
    "SettlementBatchEntity.java", "SettlementEntryEntity.java", "PayoutEntity.java", "OutboxEventEntity.java",
])
# MERGED: request(1) + response(2) -> 1
add(base, "src/main/java/com/paymentgateway/settlement/dto", ["SettlementDtos.java"])
add(base, "src/main/java/com/paymentgateway/settlement/mapper", ["SettlementBatchMapper.java", "PayoutMapper.java"])
# MERGED: 4 exceptions -> 1
add(base, "src/main/java/com/paymentgateway/settlement/exception", ["SettlementExceptions.java", "GlobalExceptionHandler.java"])
add(base, "src/main/java/com/paymentgateway/settlement/security", ["OperatorRoleAuthFilter.java"])
add(base, "src/main/java/com/paymentgateway/settlement/validation", ["SettlementValidator.java", "BankAccountValidator.java"])
# MERGED: 3 consumers -> 1
add(base, "src/main/java/com/paymentgateway/settlement/event/consumer", ["PlatformEventConsumer.java"])
# MERGED: 2 schedulers -> 1
add(base, "src/main/java/com/paymentgateway/settlement/scheduler", ["SettlementMaintenanceScheduler.java"])
add(base, "src/main/java/com/paymentgateway/settlement/constant", ["SettlementConstants.java"])
files.append(f"{pkg}/SettlementServiceApplication.java")

std_resources(base, "settlement-service", [
    "create_settlement_batch_table", "create_settlement_entry_table",
    "create_payout_table", "create_outbox_event_table",
])
std_docker_k8s(base, "settlement-service")
std_test_mirror(base, "settlement",
    unit_tests=["calculation/FeeCalculatorTest.java", "domain/batch/SettlementBatchTest.java",
                "scheduling/ScheduleManagerTest.java"],
    integration_tests=["BatchSettlementIntegrationTest.java",
                        "DuplicateBatchPreventionIntegrationTest.java",
                        "PayoutReconciliationIntegrationTest.java"])

# ---------------------------------------------------------------------------
# BROWSER SDK (unchanged - not part of the Java boilerplate problem)
# ---------------------------------------------------------------------------
sdk = "browser-sdk"
for f in [
    "package.json", "README.md", "webpack.config.js",
    "src/core/PaymentSDK.js", "src/core/init.js", "src/core/eventEmitter.js",
    "src/iframe/secureFieldRenderer.js", "src/iframe/postMessageBridge.js",
    "src/validation/clientSideValidator.js", "src/tokenization/tokenizationClient.js",
    "src/netbanking/redirectHandler.js", "src/errors/errorTranslator.js", "src/metrics/beacon.js",
    "iframe-host/index.html", "iframe-host/iframeApp.js", "dist/.gitkeep",
    "test/unit/clientSideValidator.test.js", "test/unit/errorTranslator.test.js",
    "test/integration/tokenizationFlow.test.js",
]:
    files.append(f"{sdk}/{f}")

# ---------------------------------------------------------------------------
# PROVIDER SIMULATORS — CONSOLIDATED: 5 near-duplicate modules -> 1 profile-driven module
# ---------------------------------------------------------------------------
sim = "provider-simulator"
files.append(f"{sim}/pom.xml")
files.append(f"{sim}/Dockerfile")
files.append(f"{sim}/.dockerignore")
files.append(f"{sim}/src/main/java/com/paymentgateway/simulator/SimulatorApplication.java")
files.append(f"{sim}/src/main/java/com/paymentgateway/simulator/controller/SimulatorController.java")
files.append(f"{sim}/src/main/java/com/paymentgateway/simulator/config/ScenarioProfileConfig.java")
files.append(f"{sim}/src/main/resources/application.yml")
for profile in ["providera", "providerb", "providerc", "providerd", "netbanking"]:
    files.append(f"{sim}/src/main/resources/application-{profile}.yml")
for scenario in ["approve", "decline", "timeout"]:
    files.append(f"{sim}/src/main/resources/scenarios/{scenario}.json")

# ---------------------------------------------------------------------------
# INFRA (unchanged)
# ---------------------------------------------------------------------------
files.append("infra/kubernetes/base/kustomization.yaml")
for ns in ["api-gateway", "merchant-service", "vault", "payment-orchestrator",
           "acquiring-adapter", "webhook-service", "settlement-service"]:
    files.append(f"infra/kubernetes/namespaces/{ns}-namespace.yaml")
files.append("infra/kubernetes/namespaces/vault-networkpolicy.yaml")

for env in ["dev", "staging", "uat", "production"]:
    files.append(f"infra/kubernetes/overlays/{env}/kustomization.yaml")
    files.append(f"infra/kubernetes/overlays/{env}/replica-patch.yaml")
    files.append(f"infra/kubernetes/overlays/{env}/resource-limits-patch.yaml")

files.append("infra/helm/payment-gateway/Chart.yaml")
files.append("infra/helm/payment-gateway/values.yaml")
files.append("infra/helm/payment-gateway/values-production.yaml")
files.append("infra/helm/payment-gateway/templates/.gitkeep")

files.append("infra/docker/postgres/init-multiple-databases.sh")
files.append("infra/docker/kafka/topics-init.sh")

for dash in ["traffic-overview", "cryptographic-performance", "onboarding-funnel",
             "payment-health", "provider-health", "delivery-health",
             "settlement-health", "security-posture"]:
    files.append(f"infra/grafana/dashboards/{dash}.json")
files.append("infra/grafana/provisioning/datasources.yaml")
files.append("infra/grafana/provisioning/dashboards.yaml")

files.append("infra/prometheus/prometheus.yml")
files.append("infra/prometheus/alert-rules.yml")
files.append("infra/prometheus/recording-rules.yml")

# ---------------------------------------------------------------------------
# SCRIPTS (unchanged)
# ---------------------------------------------------------------------------
files.append("scripts/local-bootstrap.sh")
files.append("scripts/run-all-migrations.sh")
files.append("scripts/load-test/gatling-authorization-scenario.scala")
files.append("scripts/load-test/k6-tokenize-scenario.js")
files.append("scripts/chaos/kill-postgres-primary.sh")
files.append("scripts/chaos/inject-hsm-kms-latency.sh")

# ---------------------------------------------------------------------------
# BUILD
# ---------------------------------------------------------------------------
for f in files:
    full = os.path.join(ROOT, f)
    d = os.path.dirname(full)
    if d:
        os.makedirs(d, exist_ok=True)
    with open(full, "a"):
        pass

print(f"Created {len(files)} files under {ROOT}/")