/*
 * Why do we need this class?
 *
 * This class generates a new Correlation ID (A Correlation ID is a unique
 * request identifier that travels with a request across multiple
 * microservices. It helps developers trace and debug the complete journey
 * of a request from start to finish.) whenever an incoming request does
 * not already contain one.
 *
 * Instead of every service generating Correlation IDs in its own way,
 * the entire platform uses this single utility class, ensuring a
 * consistent format across all microservices.
 *
 * Why we need it:
 * It makes request tracing, logging, and debugging much easier in a
 * distributed system where one request passes through multiple services.
 */

/*
 * Simple Flow:
 *
 * Client Request
 *        |
 *        v
 * Correlation ID Present?
 *        |
 *   Yes ------> Use the existing Correlation ID
 *        |
 *       No
 *        |
 *        v
 * Generate a new UUID
 *        |
 *        v
 * Attach it to the request
 *        |
 *        v
 * Use the same Correlation ID across all microservices
 */