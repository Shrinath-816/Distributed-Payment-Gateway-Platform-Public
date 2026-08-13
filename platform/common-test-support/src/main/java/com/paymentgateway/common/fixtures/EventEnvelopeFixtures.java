/*
 * What are Fixtures, Unit Tests, Integration Tests, and Contract Tests?
 *
 * Fixture -> Ready-made sample data used during testing.
 *
 * Unit Test -> Tests a single class or method in isolation.
 *
 * Integration Test -> Tests how multiple classes or services work
 * together.
 *
 * Contract Test -> Verifies that different microservices exchange data
 * in the expected format.
 */

/*
 * Why do we need this class?
 *
 * This class provides ready-made sample EventEnvelope objects that can be
 * reused in unit tests, integration tests, and contract tests across all
 * microservices.
 *
 * (A Fixture is simply predefined sample test data used during testing,
 * so developers don't have to create the same objects repeatedly.)
 *
 * Instead of every service creating its own sample events, this shared
 * class provides one consistent set of test data for the entire platform.
 *
 * Why we need it:
 * It reduces duplicate test code, keeps test data consistent across
 * services, and makes writing tests much faster.
 */

/*
 * Overall Flow:
 *
 * Test Starts
 *      |
 *      v
 * Call a Fixture Method
 *      |
 *      v
 * Sample EventEnvelope is Created
 *      |
 *      v
 * Pass it to the Service Under Test
 *      |
 *      v
 * Verify the Expected Result
 *
 * This avoids creating large test objects manually in every test class.
 */