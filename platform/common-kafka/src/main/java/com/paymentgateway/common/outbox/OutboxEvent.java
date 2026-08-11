/*
 * Why are we using the Outbox Pattern?
 *
 * The Outbox Pattern is a reliability pattern used when publishing Kafka
 * events. Instead of sending an event directly to Kafka, the event is
 * first saved in the database as an Outbox Record. A separate Outbox Relay
 * later reads these records and publishes them to Kafka.
 *
 * This record represents one row in the Outbox table. It contains all the
 * information required to publish a single event.
 *
 * Why we need it:
 * It guarantees that business data and the corresponding Kafka event are
 * stored together in the same database transaction, preventing event loss
 * if the application crashes before publishing to Kafka.
 */

/*
 * Important Fields:
 *
 * id -> A unique identifier for this Outbox record.
 *
 * eventType -> The type of business event that will be published,
 *              such as PaymentCreated or MerchantRegistered.
 *
 * aggregateId -> The ID of the business entity related to this event.
 *                It is also used as the Kafka partition key so related
 *                events are delivered in the correct order.
 *
 * payload -> The complete Event Envelope already converted into a JSON
 *            string and ready to be published to Kafka.
 *
 * status -> Tracks the current state of the Outbox record, such as
 *           Pending, Published, or Failed.
 *
 * createdAt -> Stores the UTC time when this Outbox record was created.
 */

/*
 * Overall Flow:
 *
 * Business Request
 *        |
 *        v
 * Save Business Data
 *        |
 *        v
 * Save OutboxEvent
 *        |
 *        v
 * Commit Database Transaction
 *        |
 *        v
 * Outbox Relay Reads Pending Events
 *        |
 *        v
 * Publish Event to Kafka
 *        |
 *        v
 * Mark Outbox Record as Published
 */