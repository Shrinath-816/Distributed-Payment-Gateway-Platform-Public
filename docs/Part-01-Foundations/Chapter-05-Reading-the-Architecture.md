# Chapter 05 — Reading the Architecture

## Learning Objectives

By the end of this chapter, you will understand:

- Why software architecture diagrams exist
- How to read enterprise architecture diagrams
- How requests travel through our payment gateway
- The responsibility of every major component
- The difference between synchronous and asynchronous communication
- How data flows inside the system
- How to mentally visualize the complete architecture before writing code

---

# Introduction

Many beginners look at an architecture diagram and immediately feel overwhelmed.

Boxes.

Arrows.

Databases.

Queues.

Microservices.

Caches.

Everything seems connected to everything else.

The truth is that architecture diagrams are much easier to understand once you know **how to read them**.

An architecture diagram is simply a **map of the software system**.

Just like a road map tells you how cities are connected, an architecture diagram tells you how software components communicate with each other.

This chapter will teach you how to read enterprise architecture diagrams using the payment gateway we will build throughout this book.

---

# Why Architecture Matters

Imagine building a shopping mall without a blueprint.

The electrician starts wiring.

The plumber installs pipes.

The civil engineer pours concrete.

Soon everyone realizes that nothing fits together.

Software projects face the same problem.

Architecture provides a blueprint before development begins.

It answers questions such as:

- What components do we need?
- How do they communicate?
- Where is data stored?
- What happens if one service fails?
- How do we scale the system?

Without architecture, large software projects become difficult to maintain and nearly impossible to scale.

---

# The High-Level Architecture

Below is the simplified architecture of our payment gateway.

```text
                 Customer
                     │
                     ▼
            Merchant Application
                     │
                     ▼
             API Gateway / Load Balancer
                     │
        ┌────────────┴────────────┐
        ▼                         ▼
 Authentication Service     Payment Service
                                      │
                                      ▼
                             Fraud Detection
                                      │
                                      ▼
                              Payment Router
                                      │
                       ┌──────────────┴──────────────┐
                       ▼                             ▼
               Processor A                   Processor B
                       │                             │
                       └──────────────┬──────────────┘
                                      ▼
                                  Kafka
                                      │
        ┌───────────────┬─────────────┼─────────────┐
        ▼               ▼             ▼             ▼
 Notification     Audit Logs    Analytics     Settlement
                                      │
                                      ▼
                               PostgreSQL
                                      │
                                      ▼
                                   Redis
```

Do not worry if every component is unfamiliar.

We will study each one individually.

---

# Reading an Architecture Diagram

Never try to understand everything at once.

Instead, follow three simple questions.

## 1. Where does the request begin?

In our system:

```
Customer
```

Every request starts here.

---

## 2. Where does the request travel?

The customer sends the request to:

```
Merchant Application

↓

API Gateway

↓

Payment Service
```

Always follow the arrows.

The arrows show the direction of communication.

---

## 3. Where does the request end?

Eventually the payment reaches:

- Payment Processor
- Bank
- Database
- Notification Services

Once processing completes, the response returns to the customer.

---

# Step-by-Step Request Flow

Let's understand the complete journey.

---

## Step 1 — Customer Initiates Payment

The customer clicks:

```
Pay Now
```

The merchant application prepares a payment request.

Example:

```json
{
  "orderId": "ORD-1001",
  "amount": 5000,
  "currency": "INR"
}
```

---

## Step 2 — API Gateway Receives the Request

The API Gateway is the first server that receives incoming traffic.

Think of it as the front door of the system.

Responsibilities include:

- Authentication
- Request validation
- Rate limiting
- Routing
- Logging
- Security

Instead of exposing every internal service directly to the internet, clients communicate only with the gateway.

---

## Step 3 — Authentication Service

Before processing payments, the system verifies:

- Merchant identity
- API key
- Access token
- Permissions

If authentication fails, the request is rejected immediately.

---

## Step 4 — Payment Service

The Payment Service is the heart of our system.

Responsibilities include:

- Creating payment records
- Validating payment requests
- Managing payment states
- Coordinating downstream services
- Publishing events

Think of it as the central coordinator.

---

## Step 5 — Fraud Detection

Before any payment is processed, fraud checks are performed.

Examples:

- Duplicate payment detection
- Merchant validation
- Invalid amount checks
- Suspicious transaction patterns

Enterprise payment gateways may use machine learning for fraud detection.

Our project will implement simplified rule-based validation.

---

## Step 6 — Payment Router

Different processors have different strengths.

Some may be faster.

Some may support specific countries.

Some may be temporarily unavailable.

The Payment Router decides which processor should handle the payment.

Example:

```
Processor A

↓

Success
```

or

```
Processor A

↓

Unavailable

↓

Processor B
```

This routing logic improves reliability.

---

## Step 7 — Payment Processor

The processor communicates with external payment networks.

Responsibilities include:

- Sending authorization requests
- Receiving approval or rejection
- Handling retries
- Returning transaction status

Our project will simulate processors instead of integrating with real banking systems.

---

# What Happens After Success?

Many beginners believe the process ends after authorization.

Actually, several background tasks still remain.

These tasks should not delay the customer response.

Instead, they execute asynchronously.

---

# Kafka Event Bus

Once a payment succeeds, the Payment Service publishes an event.

Example:

```
PaymentAuthorized
```

Kafka distributes this event to interested services.

Examples include:

- Notification Service
- Audit Service
- Analytics Service
- Settlement Service

This design reduces coupling between services.

---

# Notification Service

The Notification Service sends updates to users.

Examples:

- Email
- SMS
- Push notification
- Webhook

Instead of blocking the payment request, notifications run independently.

---

# Audit Service

Financial systems require complete traceability.

The Audit Service records:

- Who initiated the payment
- When it occurred
- Current status
- Merchant details
- Transaction identifiers

Audit logs are essential for compliance and debugging.

---

# Analytics Service

Business teams require insights such as:

- Total payments
- Failed transactions
- Success rate
- Revenue trends
- Peak traffic

Instead of querying operational databases directly, analytics services process events independently.

---

# Settlement Service

Authorization reserves funds.

Settlement transfers money.

The Settlement Service manages:

- Settlement batches
- Merchant payouts
- Reconciliation
- Financial reporting

This often occurs after the customer has already received a success message.

---

# PostgreSQL

PostgreSQL stores persistent business data.

Examples:

- Merchants
- Payments
- Refunds
- Transactions
- Settlement records

Unlike Redis, PostgreSQL provides durable storage.

---

# Redis

Redis stores temporary data.

Examples:

- Cached merchant details
- Session information
- Frequently accessed configuration
- Rate-limiting counters
- Idempotency keys

Using Redis reduces database load and improves performance.

---

# Synchronous vs Asynchronous Communication

Understanding this distinction is crucial.

## Synchronous Communication

The caller waits for the response.

Example:

```
Client

↓

Payment Service

↓

Response
```

The request is complete only after the response arrives.

---

## Asynchronous Communication

The caller sends a message and continues.

Example:

```
Payment Service

↓

Kafka

↓

Notification Service
```

The Payment Service does not wait for notifications to finish.

This improves scalability and responsiveness.

---

# Request Flow vs Event Flow

Two different flows exist in enterprise systems.

## Request Flow

```
Customer

↓

Gateway

↓

Payment Service

↓

Processor
```

This flow serves the user directly.

---

## Event Flow

```
Payment Service

↓

Kafka

↓

Notification

Audit

Analytics

Settlement
```

This flow handles background processing.

Separating these concerns improves system design.

---

# Why Multiple Services?

Could we build everything inside one application?

Yes.

But as traffic grows, this creates problems:

- Difficult deployments
- Poor scalability
- Tight coupling
- Longer release cycles

Breaking functionality into services allows each component to evolve independently.

---

# Understanding the Arrows

Every arrow represents communication.

Examples:

```
HTTP Request

REST API

gRPC

Kafka Event

Database Query
```

Not all arrows represent the same type of communication.

Later chapters will explain each communication mechanism in detail.

---

# Thinking Like an Architect

Whenever you see an architecture diagram, ask:

1. Where does the request begin?
2. Which component owns this responsibility?
3. Is communication synchronous or asynchronous?
4. Where is data stored?
5. What happens if this service fails?
6. Can this component scale independently?
7. What are the dependencies?

These questions help you understand any distributed system.

---

# How This Relates to Our Project

Throughout this project, every service we build corresponds to one of the components introduced in this chapter.

Examples include:

- API Gateway
- Authentication Service
- Payment Service
- Fraud Detection
- Payment Router
- Kafka Integration
- Notification Service
- Audit Service
- Settlement Service
- PostgreSQL
- Redis

Rather than building everything at once, we will implement each component step by step while understanding its role in the overall architecture.

---

# Key Takeaways

- Architecture diagrams describe how software components interact.
- Always follow the arrows to understand request flow.
- The API Gateway is the entry point for client requests.
- The Payment Service coordinates payment processing.
- Kafka enables asynchronous communication between services.
- PostgreSQL stores permanent business data, while Redis stores temporary or frequently accessed data.
- Separating responsibilities into services improves scalability, maintainability, and reliability.
- Understanding architecture before coding leads to cleaner and more extensible systems.

---

# What's Next?

In the next chapter, we will dive deeper into **System Architecture Fundamentals**, learning concepts such as layered architecture, monoliths, microservices, service boundaries, and why modern enterprise applications are designed the way they are.