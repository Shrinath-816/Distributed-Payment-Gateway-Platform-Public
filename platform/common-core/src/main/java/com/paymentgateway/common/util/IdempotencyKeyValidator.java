/*
 * Why do we need this class?
 *
 * This class checks whether the Idempotency-Key (An Idempotency Key is a
 * unique identifier sent with a request to ensure that even if the same
 * request is sent multiple times, the operation is processed only once,
 * preventing duplicate payment processing.) is in the correct UUID format.
 *
 * This class only validates the format of the key. It does not check
 * whether the key already exists in the database or Redis. That
 * responsibility belongs to each microservice's own repository.
 *
 * Why we need it:
 * It provides one common validation method that every service can reuse,
 * keeping Idempotency-Key validation consistent across the entire platform.
 */

/*
 * Simple Flow:
 *
 * Receive Idempotency Key
 *          |
 *          v
 * Is it null or empty?
 *          |
 *   Yes ----------> Return false
 *          |
 *         No
 *          |
 *          v
 * Check UUID format
 *          |
 *   Valid ----------> Return true
 *          |
 *   Invalid --------> Return false
 */