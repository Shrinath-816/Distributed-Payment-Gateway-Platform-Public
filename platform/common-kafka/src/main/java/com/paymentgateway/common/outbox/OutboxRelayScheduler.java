/*
 * Why do we need this class?
 *
 * This class automatically starts the Outbox Relay at regular time
 * intervals. It does not contain any business logic or Kafka publishing
 * logic itself. Its only responsibility is to trigger the Outbox Relay
 * so that pending events stored in the Outbox table are continuously
 * published to Kafka.
 *
 * Why we need it:
 * It separates scheduling from business logic, making the Outbox Relay
 * easier to test, reuse, and maintain.
 */


/*
 * Overall Flow:
 *
 * Scheduler Starts Automatically
 *          |
 *          v
 * Call Outbox Relay
 *          |
 *          v
 * Read Pending Events from PostgreSQL
 *          |
 *          v
 * Publish Events to Kafka
 *          |
 *          v
 * Mark Events as Published
 *          |
 *          v
 * Write Summary to Logs
 */
