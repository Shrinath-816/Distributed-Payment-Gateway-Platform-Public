/*
 * What are JWKS and kid?
 *
 * Every JWT contains a "kid" (Key ID), which tells us which public key
 * was used to sign that token.
 *
 * The Identity Provider publishes all its public keys in a JWKS (JSON Web
 * Key Set). This class downloads those keys, stores them in memory, and
 * quickly finds the correct key using the JWT's kid value.
 *
 * This also supports key rotation. If the Identity Provider starts using
 * a new signing key, this class automatically downloads the updated JWKS
 * without requiring the application to restart.
 */

/*
 * Why do we need this class?
 *
 * This class downloads and caches the public keys (JWKS - JSON Web Key Set)
 * published by the Identity Provider. Instead of downloading the keys
 * every time a JWT arrives, it stores them in memory for a short period,
 * making JWT validation much faster.
 *
 * (JWKS - JSON Web Key Set - is a collection of public keys published by
 * the Identity Provider. These keys are used to verify the digital
 * signatures of JWTs.)
 *
 * Why we need it:
 * It improves performance by avoiding repeated network calls and
 * automatically refreshes the keys whenever they expire or new signing
 * keys are introduced.
 */

/*
 * Overall Flow:
 *
 * JWT Received
 *       |
 *       v
 * Read kid (Key ID)
 *       |
 *       v
 * Search Public Key in Cache
 *       |
 *   Found?
 *   |
 * Yes -----------> Return Public Key
 *   |
 *  No
 *   |
 *   v
 * Download Latest JWKS
 *       |
 *       v
 * Build New Cache
 *       |
 *       v
 * Return Matching Public Key
 *       |
 *       v
 * JwtValidator Uses Public Key
 *       |
 *       v
 * Verify JWT Signature
 */

