/*
 * Why are we using an Abstract Class?
 *
 * An Abstract Class is a class that cannot be created directly. It is
 * designed to be extended by other classes so they can inherit its common
 * properties and behavior.
 *
 * This class acts as the parent of all business exceptions in the platform.
 * Instead of every service creating completely different exception classes,
 * they all extend this class and automatically get the common fields like
 * ErrorCode and validation details.
 *
 * Why we need it:
 * It keeps exception handling consistent across all microservices and
 * ensures every business exception follows the same structure.
 */

/*
 * Important Variables:
 *
 * errorCode -> Stores the predefined error type (ErrorCode is an enum that
 * uniquely identifies what went wrong, such as INVALID_REQUEST or
 * MERCHANT_NOT_FOUND. It is used by the GlobalExceptionHandler to return
 * the correct HTTP response.)
 *
 * details -> Stores additional validation errors, such as which input
 * fields failed validation. If there are no validation errors, this list
 * remains empty.
 */

/*
 * Why are we extending RuntimeException?
 *
 * RuntimeException represents unchecked exceptions, meaning Java does not
 * force us to catch or declare them. Business exceptions usually extend
 * RuntimeException because they are handled centrally by Spring's
 * GlobalExceptionHandler instead of being caught in every method.
 */