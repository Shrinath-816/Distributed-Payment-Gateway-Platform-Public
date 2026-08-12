/*
 * Why do we need this class?
 *
 * This class provides the common security settings used by every
 * microservice in the platform. Instead of configuring the same security
 * options separately in every service, all services reuse this shared
 * configuration and then add their own authentication and authorization
 * rules.
 *
 * (Spring Security is the security framework provided by Spring Boot. It
 * handles authentication, authorization, session management, security
 * headers, and protection against common web attacks.)
 *
 * Why we need it:
 * It keeps security configuration consistent across all microservices,
 * avoids duplicate code, and ensures every service starts with the same
 * secure baseline.
 */


/*
 * Overall Flow:
 *
 * Service Starts
 *        |
 *        v
 * Apply Common Security Settings
 *        |
 *        +--> Disable CSRF
 *        |
 *        +--> Configure Stateless Security
 *        |
 *        +--> Enable HSTS
 *        |
 *        v
 * Service adds its own Authentication Filters
 *        |
 *        v
 * Build Final Security Configuration
 */


/*
 * Why is this called a "Base" Security Configuration?
 *
 * This class provides only the common security foundation shared by all
 * microservices. It does not decide who can access which APIs.
 *
 * Each microservice uses this baseline first and then adds its own
 * authentication filters, authorization rules, and endpoint-specific
 * security according to its business requirements.
 */