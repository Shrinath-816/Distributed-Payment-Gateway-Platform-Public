# Kafka Architecture
**Project:** Distributed Payment Gateway Platform

---

# 1. Overview

This platform follows an **Event-Driven Architecture (EDA)** using **Apache Kafka** for communication between microservices.

Instead of one service directly calling another service using REST for every operation, services publish **events** to Kafka. Other interested services consume those events and continue the business workflow independently.

This approach makes the platform:

- Highly scalable
- Loosely coupled
- Fault tolerant
- Easier to extend
- More resilient to failures

---

# 2. What is Event-Driven Architecture?

Event-Driven Architecture (EDA) is a software architecture where services communicate by exchanging **events** instead of directly invoking one another.

An **event** simply means:

> "Something important happened."

Examples:

- Merchant Registered
- Payment Created
- Payment Authorized
- Token Created
- Settlement Completed
- Webhook Delivered

Instead of asking another service what happened, services **announce** what happened.

Other services decide whether they are interested.

---

# 3. Why Kafka?

Imagine six microservices.

Without Kafka:

Merchant Service
        |
        +---- REST ----> Payment Service
        |
        +---- REST ----> Token Vault
        |
        +---- REST ----> Settlement
        |
        +---- REST ----> Webhook
        |
        +---- REST ----> Notification

Every service depends on every other service.

This creates:

- Tight coupling
- Cascading failures
- Poor scalability
- Difficult deployments

---

With Kafka:

Merchant Service

↓

Kafka

↓

Interested Services

Each service becomes independent.

---

# 4. High Level Architecture

```mermaid
flowchart LR

A[Merchant Service]
B[Token Vault]
C[Payment Orchestrator]
D[Acquiring Adapter]
E[Settlement Service]
F[Webhook Service]

K[(Apache Kafka)]

A --> K
B --> K
C --> K
D --> K
E --> K
F --> K

K --> A
K --> B
K --> C
K --> D
K --> E
K --> F