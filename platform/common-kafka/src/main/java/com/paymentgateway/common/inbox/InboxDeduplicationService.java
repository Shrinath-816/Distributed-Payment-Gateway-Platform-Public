/*
 * Why do we need this class?
 *
 * This class prevents the same Kafka event from being processed more than
 * once. Before executing any business logic, it checks whether the event
 * has already been processed. If it has, the duplicate event is ignored.
 * Otherwise, the event is marked as processed and the business logic is
 * allowed to continue.
 *
 * Why we need it:
 * Kafka can sometimes deliver the same event multiple times. This class
 * ensures that duplicate events do not cause duplicate business actions,
 * such as charging a payment or creating a settlement more than once.
 */

/*
 * Overall Flow:
 *
 * Kafka Event Received
 *          |
 *          v
 * Call tryMarkProcessed()
 *          |
 *          v
 * Event Already Processed?
 *      |
 *   Yes ----------> Skip Business Logic
 *      |
 *     No
 *      |
 *      v
 * Record Event in Inbox Table
 *      |
 *      v
 * Execute Business Logic
 *      |
 *      v
 * Commit Transaction
 */
