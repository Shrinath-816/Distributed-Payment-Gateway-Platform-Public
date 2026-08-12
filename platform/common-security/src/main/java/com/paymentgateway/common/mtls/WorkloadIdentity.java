/*
 * Why do we need this class?
 *
 * This record represents the verified identity of a microservice after
 * successful mTLS authentication. (mTLS - Mutual TLS - is a security
 * mechanism where both the client and server verify each other's identity
 * using digital certificates before communicating.)
 *
 * Instead of trusting IP addresses or service names, the platform uses
 * this verified identity to decide whether a microservice is allowed to
 * access internal APIs.
 *
 * Why we need it:
 * It provides one standard way to represent a trusted microservice
 * identity throughout the platform, making service-to-service
 * authorization secure and consistent.
 */

/*
 * Important Fields:
 *
 * identity -> The complete verified identity of the microservice
 *             (for example, "spiffe://platform/payment-orchestrator").
 *
 * serviceName -> A short and readable service name extracted from the
 *                identity, mainly used for logging and monitoring.
 */

/*
 * Overall Flow:
 *
 * Microservice connects
 *          |
 *          v
 * mTLS Certificate Verified
 *          |
 *          v
 * Extract Workload Identity
 *          |
 *          v
 * Create WorkloadIdentity Object
 *          |
 *          v
 * Extract Service Name
 *          |
 *          v
 * Use Identity for Authorization Checks
 */
