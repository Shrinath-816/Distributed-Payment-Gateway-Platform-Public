/*
 * Why do we need this class?
 *
 * This class loads the list of trusted microservices from the
 * application.yml file. Instead of hardcoding which services are allowed
 * to access internal APIs, the allow-list is stored in configuration and
 * automatically loaded when the application starts.
 *
 * (mTLS - Mutual TLS - is a security mechanism where both the client and
 * the server verify each other's identity using certificates before any
 * communication is allowed.)
 *
 * Why we need it:
 * It allows only trusted microservices to communicate with each other,
 * keeps security configuration outside the code, and makes it easy to
 * change allowed services without modifying Java code.
 */

/*
 * Overall Flow:
 *
 * Microservice Request
 *          |
 *          v
 * Verify mTLS Certificate
 *          |
 *          v
 * Extract Workload Identity
 *          |
 *          v
 * Read Allow-List from Configuration
 *          |
 *          v
 * Identity Allowed?
 *      |
 *   Yes ----------> Allow Request
 *      |
 *     No
 *      |
 *      v
 * Reject Request
 */
