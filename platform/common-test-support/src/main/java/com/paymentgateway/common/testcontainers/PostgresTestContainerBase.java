/*
 * Why do we need this class?
 *
 * This class starts a shared PostgreSQL Docker container that can be
 * reused by all integration tests across the platform.
 *
 * (PostgreSQL is the relational database used by the platform to store
 * application data such as merchants, payments, tokens, settlements,
 * outbox records, and other business information.)
 *
 * (Testcontainers is a Java library that automatically starts real Docker
 * containers during testing, allowing applications to be tested against
 * real services instead of in-memory databases or mocks.)
 *
 * Instead of every test class starting its own PostgreSQL database, all
 * tests share one container, making the test suite much faster.
 *
 * Why we need it:
 * It provides a real PostgreSQL database for integration testing so the
 * application's database operations behave exactly like they do in
 * production.
 */

/*
 * Overall Flow:
 *
 * Integration Test Starts
 *          |
 *          v
 * Load PostgresTestContainerBase
 *          |
 *          v
 * Start PostgreSQL Docker Container
 *          |
 *          v
 * Create Test Database
 *          |
 *          v
 * Register Connection Properties
 *          |
 *          v
 * Spring Boot Connects to PostgreSQL
 *          |
 *          v
 * Flyway Creates Tables
 *          |
 *          v
 * Integration Tests Read and Write Real Data
 */

/*
 * What are PostgreSQL, Testcontainers, JDBC, and R2DBC?
 *
 * PostgreSQL -> The relational database used to store application data.
 *
 * Testcontainers -> A Java library that starts real Docker containers
 * automatically during testing.
 *
 * JDBC -> The standard Java API used by traditional (blocking)
 * Spring Boot applications to communicate with a database.
 *
 * R2DBC -> A reactive, non-blocking database driver used by reactive
 * Spring Boot applications.
 */


/*
 * Example:
 *
 * Payment Orchestrator Integration Test
 *          |
 *          v
 * Extends PostgresTestContainerBase
 *          |
 *          v
 * PostgreSQL Container Starts Automatically
 *          |
 *          v
 * Spring Boot Connects to the Database
 *          |
 *          v
 * Flyway Creates All Tables
 *          |
 *          v
 * Test Saves a Payment Record
 *          |
 *          v
 * Test Reads the Same Record
 *          |
 *          v
 * Verify the Expected Result
 *
 * This allows database operations to be tested against a real PostgreSQL
 * database instead of an in-memory database or mock.
 */