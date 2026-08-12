/*
 * What are Regex and the Luhn Algorithm?
 *
 * Regex (Regular Expression) is a pattern used to search for text. Here,
 * it finds numbers that are between 12 and 19 digits long.
 *
 * The Luhn Algorithm is a checksum algorithm used by most payment card
 * numbers. Instead of masking every long number, this class first checks
 * whether the number is actually a valid card number before replacing it.
 *
 * This keeps the logs secure while avoiding unnecessary masking of normal
 * numbers like order IDs or transaction IDs.
 */

/*
 * Defense in Depth:
 *
 * This class is the last line of defense. The application should never
 * log sensitive data in the first place. If a developer accidentally logs
 * a card number, this class catches it and masks it before it reaches the
 * log file.
 */

/*
 * Why do we need this class?
 *
 * This class checks every log message before it is written to the log
 * file and automatically hides sensitive information such as card
 * numbers. (Logback is the logging framework used by Spring Boot to
 * generate application logs.)
 *
 * It scans the log message, detects anything that looks like a valid card
 * number, and replaces it with "[REDACTED]" before the log is saved.
 *
 * Why we need it:
 * It prevents accidental exposure of sensitive payment information in
 * logs and helps the platform comply with security standards like PCI-DSS.
 */

/*
 * Important Variables:
 *
 * CANDIDATE_DIGIT_RUN -> A regular expression (Regex) used to find
 *                        numbers that could potentially be card numbers.
 *
 * REDACTION_MARKER -> The text that replaces detected sensitive data.
 *
 * SAFE_FALLBACK_LINE -> A safe log message returned if anything goes
 *                       wrong during masking, ensuring sensitive data is
 *                       never written accidentally.
 */

/*
 * Overall Flow:
 *
 * Application Creates Log
 *          |
 *          v
 * Logback Generates Log Message
 *          |
 *          v
 * Scan for Possible Card Numbers
 *          |
 *          v
 * Validate Using Luhn Algorithm
 *          |
 *      Valid?
 *      |
 *   Yes ----------> Replace with "[REDACTED]"
 *      |
 *     No
 *      |
 *      v
 * Keep Original Number
 *      |
 *      v
 * Write Safe Log Message
 */

