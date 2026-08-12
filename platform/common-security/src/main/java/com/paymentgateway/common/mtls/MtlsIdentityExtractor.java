/*
 * What are SAN and CN?
 *
 * SAN (Subject Alternative Name) is the modern and recommended place to
 * store a service's identity inside a certificate.
 *
 * CN (Common Name) is an older certificate field that is used only as a
 * fallback when no SAN identity is available.
 *
 * This platform always prefers SAN because it follows modern security
 * standards, but it also supports CN for compatibility with certificates
 * that do not contain a SAN entry.
 */

/*
 * Why do we need this class?
 *
 * This class extracts the identity of a microservice from its verified
 * mTLS certificate. (A certificate is a digital document that proves the
 * identity of a service. During mTLS, both services exchange and verify
 * certificates before communicating.)
 *
 * It first tries to read the identity from the certificate's Subject
 * Alternative Name (SAN), which is the recommended location. If no SAN
 * is available, it falls back to the Common Name (CN).
 *
 * Why we need it:
 * Every internal security check requires the caller's verified identity.
 * This class provides one common way to extract that identity from a
 * trusted certificate.
 */

/*
 * Overall Flow:
 *
 * Incoming mTLS Connection
 *          |
 *          v
 * Certificate Already Verified
 *          |
 *          v
 * Read Leaf Certificate
 *          |
 *          v
 * Try Subject Alternative Name (SAN)
 *          |
 *      Found?
 *      |
 *   Yes ----------> Create WorkloadIdentity
 *      |
 *     No
 *      |
 *      v
 * Try Common Name (CN)
 *      |
 *      v
 * Create WorkloadIdentity
 *      |
 *      v
 * Return Identity for Authorization
 */