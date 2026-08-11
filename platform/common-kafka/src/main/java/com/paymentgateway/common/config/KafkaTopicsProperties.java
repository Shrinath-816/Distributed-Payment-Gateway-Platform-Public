/*
 * Why do we need this class?
 *
 * This class stores the names of all Kafka Topics. (A Kafka Topic is a
 * logical channel where producers publish events and consumers read those
 * events. You can think of it as a message queue for a specific type of
 * event.)
 *
 * Instead of writing topic names like "payment.events" or
 * "merchant.events" directly inside the code, every service reads them
 * from the application's configuration file (application.yml) through
 * this class.
 *
 * Why we need it:
 * It keeps topic names centralized, avoids hardcoding values in multiple
 * places, and allows different environments (Development, Testing,
 * Staging, Production) to use different topic names without changing
 * the source code.
 */

/*
 * Important Variables:
 *
 * Each variable stores the name of one Kafka Topic used by the platform.
 *
 * Example:
 * merchantEvents   -> Topic for merchant-related events.
 * paymentEvents    -> Topic for payment-related events.
 * vaultEvents      -> Topic for token vault events.
 * settlementEvents -> Topic for settlement events.
 *
 * These default values can be overridden from application.yml without
 * modifying the Java code.
 */

/*
 * Overall Flow:
 *
 * application.yml
 *        |
 *        v
 * kafka.topics
 *        |
 *        v
 * Spring Boot reads the configuration
 *        |
 *        v
 * KafkaTopicsProperties object is created
 *        |
 *        v
 * KafkaProducerConfig and KafkaConsumerConfig use these topic names
 *        |
 *        v
 * Messages are published to and consumed from the correct Kafka Topics
 */