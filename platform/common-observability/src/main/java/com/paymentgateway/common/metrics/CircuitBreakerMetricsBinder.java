/*
 * Why do we need this class?
 *
 * This class automatically monitors every Circuit Breaker used by the
 * microservices and publishes its current state as a metric.
 *
 * (A Circuit Breaker is a fault-tolerance mechanism that temporarily
 * stops sending requests to a failing service, preventing repeated
 * failures and allowing the service time to recover.)
 *
 * Instead of every microservice writing its own monitoring code, this
 * shared class exposes all Circuit Breaker states in a consistent way.
 *
 * Why we need it:
 * It helps operations teams quickly identify which downstream service is
 * unhealthy by simply looking at dashboards like Grafana, without
 * inspecting application logs.
 */

/*
 * Overall Flow:
 *
 * Service Starts
 *        |
 *        v
 * Create Circuit Breakers
 *        |
 *        v
 * This class finds every Circuit Breaker
 *        |
 *        v
 * Create a Micrometer Gauge for each one
 *        |
 *        v
 * Publish metrics to Prometheus
 *        |
 *        v
 * Grafana Dashboard
 *        |
 *        v
 * Shows:
 * Payment Provider A -> CLOSED
 * Token Vault        -> OPEN
 * Redis              -> HALF_OPEN
 *
 * If a new Circuit Breaker is created later, it is automatically added
 * without restarting the application.
 */

/*
 * What are Circuit Breaker, Resilience4j, Micrometer, and Gauge?
 *
 * Circuit Breaker -> Protects the application from repeatedly calling a
 * failing external service.
 *
 * Resilience4j -> The Java library used to implement Circuit Breakers,
 * Retry, Rate Limiter, Bulkhead, and Time Limiter.
 *
 * Micrometer -> Spring Boot's metrics library used to collect application
 * metrics.
 *
 * Gauge -> A metric that always represents the current value of something,
 * such as CPU usage, memory usage, or Circuit Breaker state.
 */

/*
 * Example:
 *
 * Payment Orchestrator calls Stripe.
 *
 * Stripe starts failing repeatedly.
 *        |
 *        v
 * Circuit Breaker becomes OPEN.
 *        |
 *        v
 * This class updates the Gauge:
 *
 * resilience4j.circuitbreaker.state
 * dependency=stripe
 * value=1
 *
 * Prometheus collects the metric.
 *        |
 *        v
 * Grafana immediately shows:
 *
 * Stripe -> OPEN
 *
 * Operations engineers can quickly identify the failing dependency and
 * investigate the issue.
 */

