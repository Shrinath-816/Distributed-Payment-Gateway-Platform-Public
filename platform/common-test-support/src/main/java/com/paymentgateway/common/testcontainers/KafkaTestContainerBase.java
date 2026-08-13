/*
 * What are Testcontainers, Docker, Kafka Container, and Bootstrap Server?
 *
 * Testcontainers -> A Java library that starts real Docker containers
 * automatically during testing.
 *
 * Docker -> A platform used to run applications like Kafka and PostgreSQL
 * inside lightweight containers.
 *
 * Kafka Container -> A Docker container running a real Apache Kafka
 * broker for integration testing.
 *
 * Bootstrap Server -> The address that applications use to connect to a
 * Kafka broker.
 */

/*
 * Why do we need this class?
 *
 * This class starts a shared Kafka Docker container that can be reused by
 * all integration tests across the platform.
 *
 * (Testcontainers is a Java library that automatically starts real Docker
 * containers during testing, allowing applications to be tested against
 * real services like Kafka and PostgreSQL instead of using mocks.)
 *
 * Instead of every test class starting its own Kafka server, all tests
 * share one container, making tests faster and easier to maintain.
 *
 * Why we need it:
 * It provides a real Kafka environment for testing event-driven features
 * such as event publishing, event consumption, Outbox, and Inbox
 * processing without requiring Kafka to be installed manually.
 */

/*
 * Overall Flow:
 *
 * Integration Test Starts
 *          |
 *          v
 * Load KafkaTestContainerBase
 *          |
 *          v
 * Start Kafka Docker Container
 *          |
 *          v
 * Get Bootstrap Server Address
 *          |
 *          v
 * Register Spring Boot Property
 *          |
 *          v
 * Spring Connects to Kafka
 *          |
 *          v
 * Tests Publish and Consume Real Kafka Events
 */

/*
 * Example:
 *
 * Payment Orchestrator Integration Test
 *          |
 *          v
 * Extends KafkaTestContainerBase
 *          |
 *          v
 * Kafka Container Starts Automatically
 *          |
 *          v
 * Spring Boot Connects to Kafka
 *          |
 *          v
 * Test Publishes an Event
 *          |
 *          v
 * Another Test Consumer Reads the Event
 *          |
 *          v
 * Verify the Expected Result
 *
 * This allows the application to be tested against a real Kafka broker,
 * giving much more reliable results than using mocks.
 */

