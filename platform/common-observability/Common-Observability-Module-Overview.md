# Platform / Common Observability

## Purpose

`platform/common-observability` provides the shared observability
foundation for the Distributed Payment Gateway Platform.

It centralizes tracing, metrics, logging, and telemetry configuration so
every microservice exposes operational data consistently.

------------------------------------------------------------------------

## Why this module exists

A distributed payment gateway spans multiple microservices. If every
service configures tracing, metrics, and logging differently, debugging
and monitoring become inconsistent.

`common-observability` provides one reusable observability layer that
every service imports.

------------------------------------------------------------------------

## What belongs here

-   OpenTelemetry SDK configuration
-   Tracer configuration
-   Metrics configuration
-   OTLP exporter configuration
-   Resource attributes
-   Correlation ID utilities
-   Logging correlation helpers
-   Shared observability constants

------------------------------------------------------------------------

## What should NOT belong here

Business-specific monitoring logic should never be placed here.

Examples:

-   Merchant-specific metrics
-   Payment orchestration spans
-   Settlement dashboards
-   Business alert rules
-   Service-specific instrumentation

------------------------------------------------------------------------

## Dependency Rule

Every service can depend on `common-observability`.

`common-observability` must never depend on business modules.

------------------------------------------------------------------------

## Benefits

-   Consistent distributed tracing
-   Standardized metrics
-   Unified logging
-   Correlation ID propagation
-   Reduced duplication
-   Faster production troubleshooting
-   One trace across multiple services

------------------------------------------------------------------------

## In this project

Typical classes include:

-   `TracingConfig`
-   `MetricsConfig`
-   `ObservationConfiguration`
-   `CorrelationIdFilter`
-   `LoggingConstants`
-   `TelemetryProperties`

These components provide the shared observability foundation that
enables the platform's "one trace, one dashboard" troubleshooting
workflow.
