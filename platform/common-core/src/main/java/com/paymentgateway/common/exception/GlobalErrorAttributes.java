/*
 * Why are we extending DefaultErrorAttributes?
 *
 * DefaultErrorAttributes is a Spring Boot class that automatically creates
 * an error response whenever an unexpected exception occurs.
 *
 * This class extends it so we can replace Spring Boot's default error
 * response with our own standard platform error format. As a result,
 * every microservice returns errors in the same JSON structure, even if
 * an unexpected exception occurs.
 *
 * Why we need it:
 * It acts as the last safety net of the application. If any exception
 * is not handled elsewhere, this class ensures the client still receives
 * a clean, consistent, and secure error response instead of Spring Boot's
 * default error message.
 */

/*
 * Important Variables:
 *
 * log -> Used to record the actual exception in the server logs so
 * developers can investigate the problem later.
 *
 * GENERIC_MESSAGE -> A fixed message returned to the client instead
 * of exposing internal exception details.
 *
 * UNKNOWN_CORRELATION_ID -> Used when the incoming request does not
 * contain a Correlation ID (A Correlation ID is a unique request ID
 * that travels across all microservices, making it easier to trace
 * and debug a request from start to finish.).
 */

/*
 * What this method does:
 *
 * This method is automatically called by Spring Boot whenever an
 * unhandled exception reaches the framework.
 *
 * First, it retrieves the original exception and the Correlation ID.
 * Then it writes the real exception into the server logs for debugging.
 * Finally, it creates a standard ErrorResponse object and converts it
 * into a Map, which Spring Boot sends back to the client as the HTTP
 * error response.
 *
 * This ensures every unexpected error follows the same response format.
 */
