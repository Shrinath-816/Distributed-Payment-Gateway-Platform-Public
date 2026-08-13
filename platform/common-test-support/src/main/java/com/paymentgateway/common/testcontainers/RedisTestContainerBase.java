/*
 * Why do we need this class?
 *
 * This class starts a shared Redis Docker container that can be reused by
 * all integration tests across the platform.
 *
 * (Redis is an in-memory data store commonly used for caching,
 * idempotency, rate limiting, session storage, and other fast data
 * access use cases.)
 *
 * (Testcontainers is a Java library that automatically starts real Docker
 * containers during testing, allowing applications to be tested against
 * real services instead of mocks.)
 *
 * Instead of every test class starting its own Redis server, all tests
 * share one container, making the test suite faster and easier to
 * maintain.
 *
 * Why we need it:
 * It provides a real Redis environment so features like caching,
 * idempotency, and rate limiting can be tested exactly as they work in
 * production.
 */

/*
 * Overall Flow:
 *
 * Integration Test Starts
 *          |
 *          v
 * Load RedisTestContainerBase
 *          |
 *          v
 * Start Redis Docker Container
 *          |
 *          v
 * Get Host and Port
 *          |
 *          v
 * Register Spring Boot Properties
 *          |
 *          v
 * Spring Connects to Redis
 *          |
 *          v
 * Integration Tests Read and Write Data
 */

/*
 * What are Redis, Testcontainers, Cache, and Idempotency?
 *
 * Redis -> An in-memory database used for very fast data access.
 *
 * Testcontainers -> A Java library that starts real Docker containers
 * automatically during testing.
 *
 * Cache -> Temporarily stores frequently used data so it can be accessed
 * much faster than reading from a database every time.
 *
 * Idempotency -> Ensures that sending the same request multiple times
 * produces the same result without creating duplicate operations.
 */

/*
 * Example:
 *
 * Payment Orchestrator Integration Test
 *          |
 *          v
 * Extends RedisTestContainerBase
 *          |
 *          v
 * Redis Container Starts Automatically
 *          |
 *          v
 * Spring Boot Connects to Redis
 *          |
 *          v
 * Store an Idempotency Key
 *          |
 *          v
 * Send the Same Payment Request Again
 *          |
 *          v
 * Redis Detects the Existing Key
 *          |
 *          v
 * Duplicate Payment is Prevented
 *
 * This allows Redis-based features to be tested against a real Redis
 * server instead of using mocks.
 */