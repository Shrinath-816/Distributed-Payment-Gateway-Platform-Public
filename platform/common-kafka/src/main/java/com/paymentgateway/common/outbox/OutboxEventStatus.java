/*
 * Where is the Outbox Event stored?
 *
 * Every microservice has its own Outbox table in its own PostgreSQL
 * database. Whenever a business transaction succeeds, an OutboxEvent is
 * stored in that table as part of the same database transaction. Later,
 * the Outbox Relay reads these records and publishes them to Kafka,
 * ensuring that no event is lost even if the service crashes.
 */

/*
 * Why are we using an Enum here?
 *
 * This enum represents the different states an Outbox Event can have
 * during its journey from the database to Kafka.
 *
 * Instead of using a simple true/false flag, we use an enum because an
 * event can be waiting to be published, published successfully, or
 * permanently failed after multiple retry attempts.
 *
 * Why we need it:
 * It clearly shows the current status of every Outbox Event, making
 * monitoring, debugging, and retry handling much easier.
 */

/*
 * Overall Flow:
 *
 * Business Event Created
 *          |
 *          v
 * Save Outbox Record
 *          |
 *          v
 *      PENDING
 *          |
 *          v
 * Outbox Relay Publishes Event
 *          |
 *      Success?
 *      |
 *   Yes ------------> PUBLISHED
 *      |
 *     No
 *      |
 * Retry Limit Reached?
 *      |
 *   Yes ------------> FAILED
 */