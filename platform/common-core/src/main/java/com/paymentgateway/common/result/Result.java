/*
 * Why are we using a Sealed Interface?
 *
 * A Sealed Interface is a special Java feature that allows only specific
 * classes to implement it. This gives us better control over the possible
 * implementations and makes the code safer and easier to understand.
 *
 * This interface represents the outcome of an operation. Instead of always
 * throwing an exception when something goes wrong, an operation can return
 * either a Success or a Failure object. The caller can then decide how to
 * handle the result.
 *
 * Why we need it:
 * It provides a clean and type-safe way to represent both successful and
 * failed outcomes without relying on exceptions for every situation.
 */

/*
 * What this record does:
 *
 * Represents a successful operation and stores the value returned by that
 * operation. Whenever everything completes successfully, a Success object
 * is returned instead of throwing an exception.
 */
/*
 * What this record does:
 *
 * Represents a failed operation. Instead of throwing an exception
 * immediately, it stores the ErrorCode and message so the caller can
 * decide what to do next.
 */
/*
 * Simple Flow:
 *
 * Operation Starts
 *        |
 *        v
 *   Success ? -------- Yes -------> Return Success(value)
 *        |
 *       No
 *        |
 *        v
 * Return Failure(errorCode, message)
 *
 * Later...
 *
 * If needed, getOrThrow() converts the Failure into a normal exception.
 */