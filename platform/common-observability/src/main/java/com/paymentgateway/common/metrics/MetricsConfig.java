/*
 * Why do we need this class?
 *
 * This class configures Micrometer for every microservice in the
 * platform. (Micrometer is the metrics library used by Spring Boot to
 * collect application performance data such as request count, response
 * time, CPU usage, and custom business metrics.)
 *
 * Instead of every service configuring metrics separately, this class
 * automatically adds the same common information to every metric, such
 * as the service name and deployment environment.
 *
 * Why we need it:
 * It keeps metrics consistent across all microservices, making it easy
 * to monitor, compare, and build dashboards for the entire platform.
 */

/*
 * Overall Flow:
 *
 * Application Starts
 *         |
 *         v
 * Create Meter Registry
 *         |
 *         v
 * Read Service Name
 *         |
 *         v
 * Read Environment
 *         |
 *         v
 * Add Common Tags
 *         |
 *         v
 * Application Generates Metrics
 *         |
 *         v
 * Every Metric Automatically Includes:
 * - service
 * - environment
 *         |
 *         v
 * Prometheus / Grafana
 */

/*
 * What are Micrometer, Meter Registry, Metrics, and Tags?
 *
 * Micrometer -> The library used by Spring Boot to collect application
 * metrics.
 *
 * Meter Registry -> The central place where all metrics are registered
 * before being exported to monitoring systems.
 *
 * Metrics -> Numerical values that describe how an application is
 * performing, such as request count, response time, memory usage, and
 * error rate.
 *
 * Tags -> Additional information attached to a metric, such as the
 * service name or environment, making it easy to filter and analyze
 * metrics in tools like Grafana.
 */

/*
 * Example:
 *
 * Payment Orchestrator generates a metric:
 *
 * payment_requests_total = 250
 *
 * After this configuration, it automatically becomes:
 *
 * payment_requests_total
 * service=payment-orchestrator
 * environment=production
 *
 * Merchant Service generates:
 *
 * payment_requests_total
 * service=merchant-service
 * environment=production
 *
 * Grafana can now easily filter metrics by service or environment
 * without every microservice creating its own tagging system.
 */

