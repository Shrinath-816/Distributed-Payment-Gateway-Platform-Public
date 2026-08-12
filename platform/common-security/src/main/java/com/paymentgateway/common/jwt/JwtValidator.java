/*
 * Why do we need this class?
 *
 * This class verifies whether an incoming JWT (JSON Web Token) can be
 * trusted before allowing access to the platform. (A JWT is a digitally
 * signed token that carries information about an authenticated user or
 * client.)
 *
 * It checks the token's digital signature, expiration time, issuer,
 * audience, and other required claims. Only after all validations pass
 * are the token's details returned for further authorization checks.
 *
 * Why we need it:
 * It ensures that only valid and trusted tokens are accepted, protecting
 * the platform from forged, expired, or tampered authentication tokens.
 */

/*
 * Important Variables:
 *
 * jwksKeyCache -> Stores and provides the public keys required to verify
 *                 JWT signatures without repeatedly downloading them.
 *
 * expectedIssuer -> The trusted organization or server that must have
 *                   issued the JWT.
 *
 * expectedAudience -> Identifies which application this JWT is intended
 *                     for. Tokens created for other applications are
 *                     rejected.
 *
 * ALLOWED_ALGORITHMS -> Defines which signature algorithms are trusted.
 *                       Only RS256 and ES256 are accepted.
 *
 * CLOCK_SKEW -> Allows a small time difference between different servers
 *               when validating token expiry and activation time.
 */

/*
 * Overall Flow:
 *
 * Client Sends JWT
 *         |
 *         v
 * Parse JWT
 *         |
 *         v
 * Check Allowed Algorithm
 *         |
 *         v
 * Load Public Key from JWKS
 *         |
 *         v
 * Verify Digital Signature
 *         |
 *         v
 * Validate Time Claims
 *         |
 *         v
 * Validate Issuer & Audience
 *         |
 *         v
 * Extract Authenticated Claims
 *         |
 *         v
 * Pass Claims to Authorization
 */

