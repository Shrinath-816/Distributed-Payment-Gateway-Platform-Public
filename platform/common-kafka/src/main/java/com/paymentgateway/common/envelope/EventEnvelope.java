/*
 * Why are we using an Event Envelope?
 *
 * An Event Envelope is a standard wrapper that surrounds every event
 * published to Kafka. Instead of sending only the business data, we
 * also include important information like the event type, event ID,
 * timestamp, correlation ID, and aggregate ID.
 *
 * This class ensures every event published by every microservice follows
 * exactly the same structure. Only the payload changes depending on
 * the type of business event.
 *
 * Why we need it:
 * It provides one common event format for the entire platform, making
 * event publishing, event consumption, logging, tracing, and debugging
 * consistent across all microservices.
 */

/*
 * Why are we using Generics (<T>)?
 *
 * Generics allow this Event Envelope to carry any type of business
 * payload while keeping compile-time type safety.
 *
 * For example:
 *
 * EventEnvelope<PaymentCreatedPayload>
 * EventEnvelope<MerchantRegisteredPayload>
 * EventEnvelope<TokenCreatedPayload>
 *
 * This way, the envelope remains the same, while only the payload
 * changes for different business events.
 */

/*
 * Important Fields:
 *
 * eventId       -> A unique identifier for this particular event.
 *
 * eventType     -> Identifies what business event occurred
 *                  (PaymentCreated, MerchantRegistered, etc.).
 *
 * aggregateId   -> The ID of the business entity related to this event,
 *                  such as Payment ID or Merchant ID.
 *
 * version       -> Tracks the version of the business entity to help
 *                  with future updates and concurrency control.
 *
 * correlationId -> Connects this event to the original client request,
 *                  allowing the complete request flow to be traced across
 *                  multiple microservices.
 *
 * causationId   -> Stores the ID of the event that directly caused this
 *                  event. This helps understand chains of related events.
 *
 * timestamp     -> Records when the event was created.
 *
 * payload       -> Contains the actual business data for this event.
 */

/*
 * Overall Flow:
 *
 * Business Action Happens
 *          |
 *          v
 * Create Event Payload
 *          |
 *          v
 * Call EventEnvelope.newEvent(...)
 *          |
 *          v
 * Generate Event ID
 *          |
 *          v
 * Add Metadata (Event Type, Correlation ID, Timestamp, etc.)
 *          |
 *          v
 * Publish Event to Kafka
 *          |
 *          v
 * Other Microservices read the same Event Envelope
 */