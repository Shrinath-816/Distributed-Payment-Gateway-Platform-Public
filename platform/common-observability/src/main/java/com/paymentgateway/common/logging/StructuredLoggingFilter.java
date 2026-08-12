/*
 * What is MDC (Mapped Diagnostic Context)?
 *
 * MDC is a temporary storage used only while processing a single request.
 * Information like the correlation ID, route, response status, and request
 * time is stored here so every log message automatically includes these
 * values without passing them manually throughout the application.
 */

/*
 * Why do we need this class?
 *
 * This filter collects important information about every incoming request,
 * such as the API route, response status, and request processing time, and
 * stores it in the logging context. (MDC - Mapped Diagnostic Context - is
 * a temporary storage where information related to the current request is
 * kept so every log message automatically includes it.)
 *
 * It does not write any log messages itself. Instead, it prepares the
 * information that other logging components use when generating logs.
 *
 * Why we need it:
 * It makes application logs consistent and much easier to search,
 * troubleshoot, and trace requests across multiple microservices.
 */

/*
 * Important Variables:
 *
 * ORDER -> Decides when this filter executes in the filter chain.
 *
 * MDC_ROUTE -> Stores the API route being accessed.
 *
 * MDC_STATUS -> Stores the final HTTP response status.
 *
 * MDC_LATENCY_MS -> Stores how long the request took to complete.
 *
 * UNMATCHED_ROUTE -> Used when no API route matches the incoming request.
 */

/*
 * Overall Flow:
 *
 * Request Arrives
 *        |
 *        v
 * Record Start Time
 *        |
 *        v
 * Pass Request to Remaining Filters
 *        |
 *        v
 * Controller Executes
 *        |
 *        v
 * Response Generated
 *        |
 *        v
 * Calculate Request Latency
 *        |
 *        v
 * Store Route, Status & Latency in MDC
 *        |
 *        v
 * Logging Framework Writes Log
 *        |
 *        v
 * Clear MDC
 */
