/*
 * Simple Explanation:
 *
 * This record represents the standard error response returned by every
 * microservice whenever a request fails. (A DTO - Data Transfer Object - is
 * a simple object used to transfer data between different parts of an
 * application without containing any business logic.)
 *
 * Instead of every service creating its own error format, the entire
 * platform uses this single record. This ensures that clients always receive
 * errors in the same structure, making the API easier to understand and use.
 *
 * Why we need it:
 * Having one common error response format keeps the platform consistent,
 * simplifies debugging, and makes API integration easier for clients.
 */

/*
 * Why are we using a Record?
 *
 * A Record is a special type of class introduced in Java to store data.
 * It is mainly used for DTOs (Data Transfer Objects), where the purpose is
 * only to hold and transfer data, not to contain business logic.
 *
 * Unlike a normal class, a Record automatically creates the constructor,
 * getter methods, equals(), hashCode(), and toString() methods for us.
 * This reduces boilerplate code, makes the object immutable by default,
 * and keeps the code clean and easy to maintain.
 */