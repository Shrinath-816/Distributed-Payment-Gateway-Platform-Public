/*
 * Why do we need this class?
 *
 * This class creates the Kafka Producer configuration. (A Kafka Producer
 * is responsible for sending messages/events to Kafka topics so that
 * other microservices can consume them asynchronously.)
 *
 * It prepares everything required to publish events, such as connecting
 * to the Kafka cluster, converting Java objects into Kafka messages,
 * and configuring reliable message delivery.
 *
 * Why we need it:
 * It provides one common Kafka producer configuration that every
 * microservice can reuse, ensuring reliable and consistent event
 * publishing across the platform.
 */

/*
 * Overall Flow:
 *
 * Business Event Created
 *          |
 *          v
 * Save Event in Outbox Table
 *          |
 *          v
 * Outbox Relay Reads Event
 *          |
 *          v
 * KafkaTemplate Sends Event
 *          |
 *          v
 * Kafka Producer Publishes Event
 *          |
 *          v
 * Other Microservices Consume the Event
 */