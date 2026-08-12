/*
 * Why do we need this class?
 *
 * This class configures OpenTelemetry for every microservice in the
 * platform. (OpenTelemetry is an observability framework used to collect
 * traces, metrics, and logs from distributed applications.)
 *
 * It creates the components required to generate traces and sends them
 * to a central OpenTelemetry Collector, allowing requests to be tracked
 * across multiple microservices.
 *
 * Why we need it:
 * It gives every service the same tracing configuration, making it easy
 * to follow a single request as it travels through the entire distributed
 * payment gateway.
 */

/*
 * Overall Flow:
 *
 * Request Arrives
 *        |
 *        v
 * Tracer Starts a Span
 *        |
 *        v
 * Service Executes Business Logic
 *        |
 *        v
 * Span Collects Timing & Metadata
 *        |
 *        v
 * Span Exporter Sends Trace
 *        |
 *        v
 * OpenTelemetry Collector
 *        |
 *        v
 * Jaeger / Grafana / Zipkin (Trace Visualization)
 */

/*
 * What are OpenTelemetry, Trace, Span, OTLP, and Tracer?
 *
 * OpenTelemetry -> An open-source framework used to collect traces,
 * metrics, and logs from applications.
 *
 * Trace -> The complete journey of a request across multiple
 * microservices.
 *
 * Span -> A single step or operation within a trace (for example,
 * validating a payment or calling Kafka).
 *
 * Tracer -> The object used by developers to create spans.
 *
 * OTLP (OpenTelemetry Protocol) -> The standard protocol used to send
 * trace data from applications to the OpenTelemetry Collector.
 */

/*
 * Example:
 *
 * Customer makes a payment.
 *
 * API Gateway
 *      |
 *      v
 * Merchant Service
 *      |
 *      v
 * Token Vault
 *      |
 *      v
 * Payment Orchestrator
 *      |
 *      v
 * Acquiring Adapter
 *      |
 *      v
 * Settlement Service
 *
 * This entire journey is one Trace.
 *
 * Each service creates its own Span, allowing developers to see exactly
 * where time was spent or where a failure occurred.
 */

