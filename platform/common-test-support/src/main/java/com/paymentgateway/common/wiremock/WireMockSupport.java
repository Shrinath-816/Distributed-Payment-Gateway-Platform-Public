/*
 * Why do we need this class?
 *
 * This class starts a local WireMock server that simulates external HTTP
 * services during integration testing.
 *
 * (WireMock is a testing tool that acts like a fake HTTP server. Instead
 * of calling a real payment provider, bank, or HSM, the application sends
 * requests to WireMock, which returns predefined responses.)
 *
 * Instead of depending on real external systems that may be unavailable,
 * slow, or expensive, tests can run quickly and produce predictable
 * results.
 *
 * Why we need it:
 * It allows integration tests to verify HTTP communication, retry logic,
 * timeout handling, and error scenarios without making actual network
 * calls.
 */


/*
 * Overall Flow:
 *
 * Integration Test Starts
 *          |
 *          v
 * Start WireMock Server
 *          |
 *          v
 * Create Fake API Responses (Stubs)
 *          |
 *          v
 * Application Sends HTTP Request
 *          |
 *          v
 * WireMock Receives Request
 *          |
 *          v
 * Returns the Predefined Response
 *          |
 *          v
 * Application Continues Normally
 *          |
 *          v
 * Test Verifies the Result
 */

/*
 * What are WireMock, Stub, Timeout, and Sequential Responses?
 *
 * WireMock -> A tool that creates a fake HTTP server for testing.
 *
 * Stub -> A predefined fake response returned when a specific request is
 * received.
 *
 * Timeout -> A delayed response used to test how the application handles
 * slow external services.
 *
 * Sequential Responses -> Different responses returned one after another
 * for repeated requests, useful for testing retry logic.
 */

/*
 * Example:
 *
 * Payment Orchestrator
 *          |
 *          v
 * Sends Payment Request
 *          |
 *          v
 * WireMock (Fake Stripe API)
 *          |
 *          +---- First Request -----> HTTP 500
 *          |
 *          +---- Second Request ----> HTTP 500
 *          |
 *          +---- Third Request -----> HTTP 200 Success
 *          |
 *          v
 * Retry Logic Executes
 *          |
 *          v
 * Payment Successfully Completes
 *
 * This allows retry, timeout, and Circuit Breaker behavior to be tested
 * without calling the real payment provider.
 */


/*
 * Why do we use WireMock instead of calling the real payment provider?
 *
 * Real external services may be slow, unavailable, rate-limited, or may
 * charge money for every request.
 *
 * WireMock gives complete control over the responses, allowing developers
 * to easily test success, failure, timeout, retry, and error scenarios
 * in a fast and reliable way.
 */