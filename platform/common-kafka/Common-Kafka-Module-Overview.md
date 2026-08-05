# Platform / Common Kafka

## Purpose

`platform/common-kafka` provides the shared event-driven messaging
foundation for the Distributed Payment Gateway Platform.

Instead of every microservice configuring Apache Kafka independently,
this module centralizes reusable messaging infrastructure, conventions,
and utilities.

------------------------------------------------------------------------

## Why this module exists

The platform relies on asynchronous communication between microservices.
Duplicating Kafka configuration, serializers, topic conventions, and
event models across services would lead to inconsistency and unnecessary
maintenance.

`common-kafka` provides a single, reusable messaging layer that every
service can depend on.

------------------------------------------------------------------------

## What belongs here

This module contains reusable Kafka infrastructure, such as:

-   Kafka producer and consumer configuration
-   Common event models
-   Topic constants
-   Serialization/deserialization utilities
-   Dead Letter Queue (DLQ) helpers
-   Shared message headers
-   Event publishing interfaces
-   Shared Kafka exceptions

------------------------------------------------------------------------

## What should NOT belong here

Business workflows and service-specific consumers/producers must not be
placed here.

Examples: - Merchant onboarding workflow - Payment orchestration -
Settlement processing - Token vault logic - Business event handlers

------------------------------------------------------------------------

## Dependency Rule

Every service can depend on `common-kafka`.

`common-kafka` must never depend on any business service.

------------------------------------------------------------------------

## Benefits

-   Standardized event-driven communication
-   Centralized Kafka configuration
-   Reusable messaging infrastructure
-   Reduced duplication
-   Easier maintenance
-   Consistent event contracts
-   Faster service development

------------------------------------------------------------------------

## In this project

Typical classes include:

-   `KafkaConfiguration`
-   `KafkaTopics`
-   `EventPublisher`
-   `EventEnvelope`
-   `KafkaHeaderNames`
-   `DeadLetterHandler`

These classes form the shared messaging foundation for the platform.
