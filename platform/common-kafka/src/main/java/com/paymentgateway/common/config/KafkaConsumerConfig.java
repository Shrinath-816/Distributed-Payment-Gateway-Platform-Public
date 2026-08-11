/*
 * Why do we need this class?
 *
 * This class creates the Kafka Consumer configuration. (Apache Kafka is a
 * distributed messaging platform that allows microservices to communicate
 * asynchronously by sending and receiving events instead of calling each
 * other directly.)
 *
 * It prepares everything required for a service to consume messages from
 * Kafka, such as connecting to the Kafka cluster, deserializing incoming
 * messages, handling invalid messages, and controlling when a message is
 * marked as successfully processed.
 *
 * Why we need it:
 * It provides one common Kafka consumer configuration that every
 * microservice can reuse, ensuring reliable and consistent event
 * consumption across the platform.
 */

/*
 * Important Variables:
 *
 * bootstrapServers -> Stores the address of the Kafka cluster that this
 *                     service connects to.
 *
 * groupId -> Identifies the Consumer Group (A Consumer Group is a set of
 *            consumers working together to process messages from the same
 *            topic. Kafka automatically distributes messages among the
 *            consumers in the same group.)
 */


/*
 * Overall Flow:
 *
 * Kafka Topic
 *      |
 *      v
 * Consumer connects to Kafka
 *      |
 *      v
 * Read Message
 *      |
 *      v
 * Convert bytes into Java object (Deserialization)
 *      |
 *      v
 * Process Business Logic
 *      |
 *      v
 * Success?
 *      |
 * Yes ---------> Manually acknowledge the message
 *      |
 * No
 *      |
 * Error Handler logs the problem and handles it safely
 */