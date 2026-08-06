# KafkaConsumerConfig.java
Relative Path: platform/common-kafka/src/main/java/com/paymentgateway/common/config/KafkaConsumerConfig.java

1. File Overview

Module: common-kafka | Package: common.config | Layer: Infrastructure
Doc Cross-Reference: Event-Catalog.md §6 (Consumer Groups), §9 (Delivery Guarantees)
2. Purpose

Shared Kafka consumer factory configuration (manual offset commit after successful Inbox-deduped processing, deserialization error handling) each service's event consumer classes build on.
3. Responsibilities

Configures manual acknowledgment mode (offset committed only after the consuming service's local transaction — business write + Inbox record — succeeds, per SYSTEM_DESIGN.md §7's at-least-once guarantee), a deserialization error handler that routes an unparseable message to logging/alerting rather than crashing the consumer thread.
Must NOT configure a specific consumer group ID — each service names its own consumer group per its own bounded context, supplied via that service's own application.yml, not hardcoded here.
4. Dependencies

Internal: KafkaTopicsProperties.
External: Spring Kafka ConsumerFactory/ConcurrentKafkaListenerContainerFactory.
5. Public API

@Bean method producing a shared ConsumerFactory/listener container factory configured as above.
6. Internal Workflow

Builds consumer properties map (manual ack mode, deserializer, error handler), constructs the factory bean.
7. Engineering Considerations

Reliability: manual-ack-after-business-write is what makes the platform's Inbox dedupe guarantee actually hold — an auto-commit configuration would risk committing an offset before the corresponding business effect is durably recorded.
8. Testing Strategy

Integration test verifying a consumer only commits its offset after the (test) business handler completes successfully, and that a handler exception leaves the offset uncommitted for redelivery.
9. Future Extension

If a dead-letter-topic pattern is ever adopted (currently the platform explicitly does not use one, per Event-Catalog.md §8), this is where it would be introduced.
# OutboxRelay.java
Relative Path: platform/common-kafka/src/main/java/com/paymentgateway/common/outbox/OutboxRelay.java

1. File Overview

Module: common-kafka | Package: common.outbox | Layer: Shared kernel / application-adjacent infrastructure
Doc Cross-Reference: SYSTEM_DESIGN.md §7 (Outbox Pattern)
2. Purpose

The single, reusable polling-and-publishing engine every service's own Outbox adapter delegates to, guaranteeing the exact same "read unpublished rows, publish, mark published" behavior platform-wide rather than seven independent re-implementations.
3. Responsibilities

Defines a port interface, OutboxEventStorePort (in this same package), with methods each service's own persistence adapter implements: find a batch of PENDING rows, mark a row PUBLISHED, mark a row FAILED after exhausted attempts.
On each invocation (triggered by OutboxRelayScheduler), fetches a bounded batch of pending rows via the port, publishes each to Kafka via the shared KafkaProducerConfig-provided template, and marks each row's outcome via the port.
Must NOT itself contain any JPA/database-specific code — persistence is entirely behind OutboxEventStorePort, implemented per-service in Phase 1+.
4. Dependencies

Internal: OutboxEvent, OutboxEventStatus, EventEnvelope (for deserializing the stored JSON payload back into a publishable message, or simply forwarding the pre-serialized string as the Kafka message value — the latter is simpler and avoids a redundant deserialize/reserialize round trip, and is the recommended approach).
External: Spring KafkaTemplate.
5. Public API

OutboxEventStorePort interface methods: List<OutboxEvent> findPendingBatch(int batchSize); void markPublished(UUID outboxEventId); void markFailed(UUID outboxEventId, String reason).
int pollAndPublish(int batchSize) — Purpose: the relay's core cycle; Parameters: how many rows to fetch per invocation; Return: count of rows successfully published; Exceptions: does not propagate individual publish failures (caught, logged, row marked FAILED or left PENDING for retry depending on failure classification) — only propagates a genuine infrastructure-level failure (e.g. the store port itself is unreachable) to the caller/scheduler.
6. Internal Workflow

Fetch a bounded batch of PENDING rows via the store port.
For each row: publish its pre-serialized payload to the topic derived from its eventType (via KafkaTopicsProperties' event-type-to-topic mapping, resolved by a small internal lookup), keyed by aggregateId.
On successful publish acknowledgment, call markPublished.
On a transient publish failure, leave the row PENDING (next poll cycle retries it) up to a bounded number of cycles; on repeated failure beyond that bound, call markFailed and emit an alert-worthy log/metric.
7. Engineering Considerations

Reliability: this is the component that makes the platform's "no event is ever lost" guarantee concrete — a row is never removed from PENDING until Kafka has acknowledged it.
Performance: batched polling with a partial index on published=false-equivalent (status=PENDING) is assumed at the persistence layer per every service's own database design docs — this class's batchSize parameter exists specifically to keep each poll cycle bounded and fast regardless of historical table volume.
8. Testing Strategy

Unit test with a fake OutboxEventStorePort and a mocked Kafka template: successful batch publish marks all rows published; a simulated publish failure leaves the row pending; repeated failures beyond the bound mark it failed.
Integration test (Testcontainers Kafka) verifying an end-to-end publish actually lands on the expected topic/partition.
9. Future Extension

If per-event-type publish prioritization is ever needed, findPendingBatch could accept an ordering/priority hint — not implemented now (no demonstrated need).
# OutboxRelayScheduler.java
Relative Path: platform/common-kafka/src/main/java/com/paymentgateway/common/outbox/OutboxRelayScheduler.java

1. File Overview

Module: common-kafka | Package: common.outbox | Layer: Infrastructure (scheduled trigger)
Doc Cross-Reference: SYSTEM_DESIGN.md §7 (Outbox Pattern)
2. Purpose

The thin, @Scheduled-annotated trigger that periodically invokes OutboxRelay.pollAndPublish(...), kept separate from OutboxRelay itself so the relay's core logic remains framework-scheduling-agnostic and independently unit-testable.
3. Responsibilities

Invokes OutboxRelay.pollAndPublish(batchSize) on a fixed-delay schedule, both values externalized as properties (not hardcoded).
Must NOT contain any publishing or persistence logic itself — purely a timing trigger.
4. Dependencies

Internal: OutboxRelay.
External: Spring @Scheduled.
5. Public API

Scheduled method triggerRelay() — Purpose: the @Scheduled entry point; Parameters: none; Return: void; Exceptions: none escape (delegates entirely to OutboxRelay, which already handles its own failure classification).
6. Internal Workflow

On each tick, call outboxRelay.pollAndPublish(configuredBatchSize) and log the returned publish count at INFO.
7. Engineering Considerations

Each service configures its own fixed-delay interval appropriate to its own publish-latency SLO (e.g. tighter for Payment Orchestrator's payment.events, looser for lower-urgency topics) via its own application.yml, without needing a different Java class.
8. Testing Strategy

Unit test verifying the scheduled method calls pollAndPublish with the configured batch size.
9. Future Extension

None anticipated; deliberately kept minimal per the Simplicity First principle.
# InboxDeduplicationService.java
Relative Path: platform/common-kafka/src/main/java/com/paymentgateway/common/inbox/InboxDeduplicationService.java

1. File Overview

Module: common-kafka | Package: common.inbox | Layer: Shared kernel / application-adjacent infrastructure
Doc Cross-Reference: SYSTEM_DESIGN.md §7 (Inbox Pattern)
2. Purpose

The single, reusable "have I already processed this event" check every service's Kafka consumer calls before executing its business handler, the consumer-side counterpart to OutboxRelay.
3. Responsibilities

Defines a port interface, InboxEventStorePort, with methods each service's persistence adapter implements: check-and-record an eventId as processed within the same local transaction as the business handler's own write.
Must NOT execute the business handler itself — this service only answers "already processed? yes/no" and, on "no," records the eventId; the calling consumer class (per-service, later phases) is responsible for wrapping both the business write and this record call in one local transaction.
4. Dependencies

Internal: InboxEvent.
External: none beyond the port interface.
5. Public API

InboxEventStorePort interface method: boolean tryMarkProcessed(UUID eventId, String consumerName) — Purpose: atomically check-and-insert; Parameters: the event's ID and the logical consumer name; Return: true if this call newly recorded it (caller should proceed with business processing), false if it was already recorded (caller should skip processing, since another delivery already handled it); Exceptions: implementation-specific (data-access exceptions propagate — a failure here must abort the whole transaction, since proceeding without a successful dedupe record would risk double-processing).
6. Internal Workflow

Delegates directly to the injected InboxEventStorePort's atomic check-and-insert (implemented per-service against a unique-constrained inbox_event table in Phase 1+, using an INSERT ... ON CONFLICT DO NOTHING-equivalent or a caught unique-constraint-violation pattern to achieve atomicity without a separate SELECT-then-INSERT race).
7. Engineering Considerations

Concurrency: the atomicity of "check and record" must be a single database operation, not a read-then-write pair, to avoid a race between two concurrent deliveries of the same (redelivered) message.
Correctness: this must run in the same local transaction as the business handler's own write (both committed together, or both rolled back together) — a detail each service's consumer class (Phase 1+) is responsible for wiring correctly; this class only exposes the atomic primitive.
8. Testing Strategy

Unit test with a fake InboxEventStorePort: first call for a given eventId returns true; second call for the same ID returns false.
Integration test simulating two concurrent redelivery attempts, asserting exactly one succeeds in marking processed.
9. Future Extension

If a future need arises to expire old inbox records (mirroring IdempotencyRecordCleanupJob), a similar shared cleanup job could be added here following the same pattern — not implemented now, since inbox retention policy hasn't yet been specified in any service's own doc.
