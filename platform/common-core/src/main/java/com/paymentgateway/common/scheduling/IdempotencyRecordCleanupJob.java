/*
 * Why do we need this class?
 *
 * This class automatically removes old Idempotency Records (Idempotency
 * means that sending the same request multiple times should produce only
 * one successful operation, preventing duplicate payment processing.)
 * after they have expired.
 *
 * Instead of every microservice writing its own cleanup scheduler, the
 * entire platform shares this single cleanup job. Every service simply
 * provides its own database implementation, and this class performs the
 * cleanup automatically at regular intervals.
 *
 * Why we need it:
 * It keeps the database clean, prevents unnecessary storage growth, and
 * avoids duplicate cleanup logic across multiple microservices.
 */

/*
 * Important Variables:
 *
 * purgePorts -> Stores all registered cleanup implementations provided
 *               by different microservices.
 *
 * ttl -> (Time To Live) defines how long an Idempotency Record should
 *        remain in the database before it is considered expired.
 *
 * purgedCounter -> A Micrometer Counter (Micrometer is Spring Boot's
 *                  metrics library used to collect application statistics
 *                  like request count, cleanup count, memory usage, etc.)
 *                  that records how many expired records were removed.
 */

/*
 * Overall Flow:
 *
 * Scheduler Starts Automatically
 *            |
 *            v
 *  Read all registered cleanup implementations
 *            |
 *            v
 *  Delete expired Idempotency Records
 *            |
 *            v
 *  Update Metrics
 *            |
 *            v
 *  Write Logs
 *            |
 *            v
 *  Continue even if one cleanup fails
 */