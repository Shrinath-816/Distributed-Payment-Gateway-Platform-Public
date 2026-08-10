/*
 * Simple Explanation:
 *
 * This class stores all the common HTTP header names (HTTP Headers are small pieces
 * of information sent along with every HTTP request and response, such as authentication,
 * request identification, tracing, and content type.) used throughout the platform.
 *
 * Instead of writing header names like "Authorization" or "X-Correlation-Id" as
 * plain text in different places, every service uses the constants from this class.
 * This avoids typing mistakes, keeps the code consistent, and makes future changes easy.
 *
 * Why we need it:
 * Having a single place for all header names improves code consistency, readability,
 * and maintainability across every microservice.
 */


/*
 * Important Headers:
 *
 * AUTHORIZATION   -> Stores the Bearer Token or API credentials used to authenticate requests.
 *
 * X_API_KEY       -> Stores an API Key used mainly for secure service-to-service communication.
 *
 * IDEMPOTENCY_KEY -> A unique key (Idempotency ensures that sending the same request multiple
 *                    times produces only one successful operation.) used to prevent duplicate
 *                    payment processing.
 *
 * X_CORRELATION_ID -> A unique request ID that travels across all microservices, making it
 *                     easier to trace and debug a request.
 *
 * TRACEPARENT & TRACESTATE -> Part of the W3C Trace Context standard (used by OpenTelemetry
 *                             for Distributed Tracing, which tracks a single request as it
 *                             moves through multiple microservices).
 */