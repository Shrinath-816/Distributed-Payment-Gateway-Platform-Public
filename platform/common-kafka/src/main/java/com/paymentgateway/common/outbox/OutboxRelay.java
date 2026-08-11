/*
 * Why do we need this class?
 *
 * This class is responsible for publishing events from the Outbox table
 * to Kafka. It continuously looks for pending Outbox Events stored in
 * the database, sends them to the correct Kafka Topic, and updates their
 * status after successful publishing.
 *
 * It does not create business events or save them into the database.
 * Its only responsibility is to reliably move already stored events
 * from the Outbox table to Kafka.
 *
 * Why we need it:
 * It guarantees that no event is lost. Even if the application crashes
 * immediately after saving business data, the event remains safely stored
 * in the Outbox table and will be published when the Outbox Relay runs
 * again.
 */

/*
 * Important Variables:
 *
 * storePort -> A Port Interface used to read and update Outbox records
 *              from the database.
 *
 * kafkaTemplate -> Spring Boot's helper class used to publish messages
 *                  to Kafka.
 *
 * topicsProperties -> Stores the names of all Kafka Topics configured
 *                     in application.yml.
 *
 * TOPIC_RESOLVERS -> A lookup table that maps every Event Type to the
 *                    Kafka Topic where it should be published.
 */

/*
 * Overall Flow:
 *
 * Client Request
 *        |
 *        v
 * Save Business Data
 *        |
 *        v
 * Save Outbox Event in PostgreSQL
 *        |
 *        v
 * Commit Transaction
 *        |
 *        v
 * Outbox Relay Starts
 *        |
 *        v
 * Read Pending Outbox Events
 *        |
 *        v
 * Resolve Kafka Topic
 *        |
 *        v
 * Publish Event to Kafka
 *        |
 *        v
 * Kafka Acknowledges
 *        |
 *        v
 * Mark Outbox Event as PUBLISHED
 */
