/*
 * Why are we using the Inbox Pattern?
 *
 * The Inbox Pattern is a reliability pattern used when consuming Kafka
 * events. Its main purpose is to ensure that the same event is processed
 * only once, even if Kafka delivers it multiple times.
 *
 * This record stores the basic information of every event that has already
 * been processed. Before processing a new event, the application checks
 * whether an InboxEvent with the same Event ID already exists. If it does,
 * the duplicate event is ignored.
 *
 * Why we need it:
 * It prevents duplicate processing of the same business event, which is
 * extremely important in a payment system where processing the same event
 * twice could lead to duplicate payments, refunds, or settlements.
 */

/*
 * Overall Flow:
 *
 * Kafka Event Received
 *          |
 *          v
 * Check Inbox Table
 *          |
 *          v
 * Event Already Exists?
 *      |
 *   Yes ----------> Ignore the duplicate event
 *      |
 *     No
 *      |
 *      v
 * Process Business Logic
 *      |
 *      v
 * Save InboxEvent
 *      |
 *      v
 * Acknowledge Kafka Offset
 */
