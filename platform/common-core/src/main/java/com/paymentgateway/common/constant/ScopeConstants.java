/*
 * Simple Explanation:
 *
 * This class stores all the authorization scope names (Authorization Scope is
 * simply a permission that defines what a user or API Key is allowed to do,
 * such as reading payments or managing webhooks.) used across the platform.
 *
 * Instead of writing scope names like "payments:read" or "merchant:admin"
 * in different places, every service uses the constants from this class.
 * This ensures everyone uses the same permission names and avoids typing mistakes.
 *
 * Why we need it:
 * Keeping all scopes in one place makes permission management consistent,
 * easier to maintain, and prevents invalid scope names from being used.
 */

/*
 * Example:
 *
 * payments:read       -> Can view payment details.
 * payments:write      -> Can create, capture, refund, or modify payments.
 * merchant:admin      -> Can manage merchant account settings.
 * webhooks:manage     -> Can create or update webhook configurations.
 * settlements:read    -> Can view settlement and payout information.
 * credentials:manage  -> Can create, rotate, or revoke API credentials.
 */