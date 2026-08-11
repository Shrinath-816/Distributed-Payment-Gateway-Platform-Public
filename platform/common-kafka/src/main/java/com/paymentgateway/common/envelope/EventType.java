/*
 * Why are we using an Enum here?
 *
 * This enum stores the names of all events that can occur in the platform.
 * (An Event is simply a notification that something important has happened,
 * such as a payment being created or a merchant being registered. Other
 * microservices can listen to these events and perform their own actions.)
 *
 * Instead of writing event names as plain strings throughout the code,
 * every producer and consumer uses this enum. This avoids typing mistakes
 * and ensures everyone refers to the same event names.
 *
 * Why we need it:
 * It acts as the single source of truth for all platform events, keeping
 * event names consistent and making communication between microservices
 * reliable.
 */

/*
 * Overall Flow:
 *
 * Business Action Happens
 *           |
 *           v
 * Select an EventType
 *           |
 *           v
 * Get the wireName()
 *           |
 *           v
 * Create EventEnvelope
 *           |
 *           v
 * Publish Event to Kafka
 *           |
 *           v
 * Other Microservices receive the event
 */
