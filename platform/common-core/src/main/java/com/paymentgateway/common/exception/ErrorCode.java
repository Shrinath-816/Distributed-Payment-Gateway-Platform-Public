/*
 * Why are we using an Enum?
 *
 * An Enum (Enumeration) is a special Java type used to store a fixed set of
 * predefined constants. It ensures that only valid values can be used and
 * prevents invalid or misspelled error codes.
 *
 * This enum stores all the common error codes used across the platform.
 * Whenever an error occurs, the application uses one of these predefined
 * constants instead of writing error names as plain text.
 *
 * Why we need it:
 * It keeps error handling consistent, makes the code easier to maintain,
 * and ensures every microservice returns standardized error codes.
 */

/*
 * Important Variable:
 *
 * defaultHttpStatus -> Stores the standard HTTP status code associated
 * with each error code. (An HTTP Status Code is a three-digit number
 * returned by the server to indicate whether a request was successful
 * or why it failed, such as 200, 400, 404, or 500.)
 */

/*
 * Example:
 *
 * VALIDATION_FAILED      -> HTTP 400 (Bad Request)
 * RESOURCE_NOT_FOUND     -> HTTP 404 (Not Found)
 * RATE_LIMIT_EXCEEDED    -> HTTP 429 (Too Many Requests)
 * INTERNAL_ERROR         -> HTTP 500 (Internal Server Error)
 *
 * This mapping keeps the platform's error responses predictable and
 * consistent across every microservice.
 */