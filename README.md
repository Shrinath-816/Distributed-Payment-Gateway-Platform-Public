# Distributed Payment Gateway

A production-oriented distributed payment gateway designed to explore how modern payment systems handle reliability, consistency, failure recovery, and scale across multiple services.

**Status:** 🚧 Active development

## What I'm Building
This system is being engineered around real distributed-systems problems rather than a simple CRUD payment API.

### Core Architecture
*   Microservices-based architecture
*   **Kafka** for asynchronous event-driven communication
*   **Outbox Pattern** for reliable database → event publishing
*   **Inbox Pattern** for idempotent event consumption
*   **Saga Pattern** for distributed transaction orchestration
*   Distributed transaction management
*   **Idempotency** for safe payment retries
*   **Circuit Breakers** for isolating failing dependencies
*   Retries & backoff for transient failures
*   **Dead Letter Queues** for unrecoverable events
*   Compensation workflows for failed distributed operations
*   State-machine-driven payment lifecycle
*   Database-per-service ownership
*   Concurrency control and consistency guarantees

## Payment Flow
A typical payment can involve multiple independently failing components:

```text
Client
  │
  ▼
API Gateway
  │
  ▼
Payment Service
  │
  ├──► Order / Ledger
  │
  ├──► Fraud / Risk
  │
  ├──► Payment Provider
  │
  └──► Kafka
          │
          ▼
      Event Consumers
          │
          ▼
    Settlement / Notification
```

The goal is to ensure that failures such as timeouts, duplicate requests, service crashes, broker failures, and partial transaction completion do not leave the system in an inconsistent state.

## Reliability Engineering
The project focuses heavily on failure scenarios:

```text
Payment Request
      │
      ▼
  Idempotency
      │
      ▼
   Saga Step
      │
      ├── Success ───────► Next Step
      │
      └── Failure
            │
            ▼
      Retry / Backoff
            │
            ▼
       Compensation
            │
            ▼
      Consistent State
```

Kafka events are designed around durability and delivery guarantees, while **Outbox + Inbox patterns** address the classic problems of dual writes and duplicate message processing.

## Engineering Goals
This project is being built with a strong emphasis on:

*   Consistency over convenience
*   Failure isolation
*   Idempotent operations
*   Observable distributed workflows
*   Explicit service ownership
*   Recoverability after partial failure
*   Horizontal scalability
*   Production-oriented design

## Tech Stack
*   Java / Spring Boot
*   Apache Kafka
*   PostgreSQL
*   Redis
*   Docker
*   REST APIs
*   Distributed messaging
*   Automated testing

## Current Status
The system is being developed incrementally, with each component implemented, tested, containerized, and validated before moving forward.

This repository is a work in progress — the architecture and implementation will continue evolving as additional distributed-system capabilities are introduced.

## The Objective
Build a payment platform where failures are expected, distributed transactions are recoverable, duplicate messages are harmless, and system consistency does not depend on every service succeeding at the same time.