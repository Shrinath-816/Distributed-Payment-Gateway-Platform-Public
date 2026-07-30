# Chapter 06 — End-to-End Payment Flow

## Learning Objectives

By the end of this chapter, you will understand:

- The complete lifecycle of an online payment
- Every participant involved in processing a payment
- What happens after clicking **Pay Now**
- How requests travel through different systems
- How authorization, capture, and settlement work together
- Where failures can occur
- Why payment systems require distributed architecture

---

# Introduction

In the previous chapters, we learned about:

- What a payment gateway is
- The project vision
- Reading software architecture

Now it's time to put everything together.

In this chapter, we will follow **one payment transaction** from beginning to end.

Imagine purchasing a laptop worth **₹80,000** from an online shopping website.

Although the customer sees only a loading spinner for a few seconds, dozens of systems work together behind the scenes.

By the end of this chapter, you should be able to mentally visualize the complete journey of every online payment.

---

# The Complete Payment Journey

The entire payment lifecycle can be divided into six major phases.

```
1. Order Creation

↓

2. Payment Initiation

↓

3. Authorization

↓

4. Capture

↓

5. Settlement

↓

6. Notification & Reconciliation
```

We will study each phase individually.

---

# Phase 1 — Order Creation

Everything begins when the customer selects products.

Example:

```
Laptop

₹80,000
```

The customer clicks

```
Checkout
```

The merchant application creates an order.

Example:

```json
{
    "orderId": "ORD-10001",
    "amount": 80000,
    "currency": "INR",
    "status": "CREATED"
}
```

Notice something important.

No payment has happened yet.

Only an order exists.

---

# Why Separate Orders from Payments?

Orders and payments represent different business concepts.

An order may exist without payment.

Examples:

- Cash on Delivery
- Failed payment
- Pending payment
- Cancelled order

Therefore:

```
Order

≠

Payment
```

This separation makes systems easier to maintain.

---

# Phase 2 — Customer Initiates Payment

The customer clicks

```
Pay Now
```

The merchant application prepares a payment request.

Example:

```json
{
    "orderId": "ORD-10001",
    "amount": 80000,
    "currency": "INR",
    "paymentMethod": "CARD"
}
```

The request is sent to the Payment Gateway.

---

# API Gateway Receives the Request

The API Gateway becomes the first component inside our system.

Responsibilities:

- Authentication
- Authorization
- Request validation
- Rate limiting
- Routing
- Logging

If the request passes validation, it is forwarded to the Payment Service.

---

# Merchant Authentication

Before accepting any payment request, the gateway verifies the merchant.

Checks include:

- API Key
- Secret Key
- Access Token
- Merchant Status

If authentication fails:

```
HTTP 401

Unauthorized
```

The payment stops immediately.

---

# Payment Validation

The Payment Service validates:

- Amount
- Currency
- Merchant
- Customer
- Payment Method
- Duplicate Request
- Mandatory Fields

Example:

```
Amount > 0

✓

Currency Supported

✓

Merchant Active

✓
```

Only valid requests continue.

---

# Creating the Payment Record

The Payment Service stores a new payment.

Example:

```json
{
    "paymentId": "PAY-50001",
    "status": "CREATED"
}
```

Creating the payment before contacting external systems improves traceability.

Even if external systems fail, the payment request is not lost.

---

# Fraud Detection

Before processing the payment, fraud rules are executed.

Examples:

- Duplicate payment
- Suspicious merchant
- Invalid amount
- Rate limit exceeded
- Blacklisted customer

Enterprise systems often use machine learning for fraud detection.

Our project implements simplified rule-based validation.

---

# Payment Routing

The gateway may have multiple payment processors.

Example:

```
Processor A

Processor B

Processor C
```

The Payment Router decides which processor should receive the request.

Routing decisions may depend on:

- Availability
- Cost
- Country
- Success rate
- Payment method

---

# Phase 3 — Authorization

The processor forwards the request to the payment network.

Example:

```
Visa

Mastercard

RuPay
```

The card network identifies the issuing bank.

---

# Issuing Bank Verification

The issuing bank performs several checks.

Examples:

- Card exists
- Card active
- CVV correct
- OTP verified
- PIN validated
- Sufficient balance
- Daily limit
- Fraud detection

If every check succeeds:

```
AUTHORIZED
```

Otherwise:

```
DECLINED
```

---

# What Does Authorization Mean?

Authorization does **not** transfer money.

It simply reserves the required amount.

Example:

Customer balance

```
₹1,00,000
```

Purchase

```
₹80,000
```

Available balance becomes

```
₹20,000
```

The money is reserved but not yet transferred.

---

# Authorization Response

The response begins its return journey.

```
Issuing Bank

↓

Card Network

↓

Processor

↓

Gateway

↓

Merchant

↓

Customer
```

If approved:

```
Payment Authorized
```

---

# Updating Payment Status

The Payment Service updates the database.

```
CREATED

↓

AUTHORIZED
```

Payment states become extremely important later for reconciliation.

---

# Publishing Events

After successful authorization, an event is published.

Example:

```
PaymentAuthorized
```

Kafka distributes this event.

Subscribers include:

- Notification Service
- Audit Service
- Analytics Service
- Settlement Service

---

# Phase 4 — Capture

Some businesses capture immediately.

Others capture later.

Examples:

Immediate Capture

- Shopping websites

Delayed Capture

- Hotels
- Airlines
- Car rentals

Capture actually instructs the bank to collect the reserved money.

Payment state changes:

```
AUTHORIZED

↓

CAPTURED
```

---

# Phase 5 — Settlement

Now the money starts moving.

Flow:

```
Customer Bank

↓

Card Network

↓

Acquiring Bank

↓

Merchant Account
```

Settlement often occurs in batches.

Examples:

T+0

T+1

T+2

depending on banking rules.

---

# Settlement Record

Example:

```json
{
    "settlementId": "SET-70001",
    "amount": 80000,
    "status": "SETTLED"
}
```

The merchant finally receives the money.

---

# Phase 6 — Notifications

The customer receives:

```
Payment Successful
```

The merchant receives:

```
Payment Received
```

Notifications may be sent through:

- Email
- SMS
- Push Notification
- Webhook

These happen asynchronously.

---

# Audit Logging

Every payment action is recorded.

Example:

```
Payment Created

↓

Authorized

↓

Captured

↓

Settled
```

Audit logs help during:

- Compliance
- Investigations
- Customer disputes
- Debugging

---

# Reconciliation

Banks and payment gateways periodically compare records.

Questions include:

Did every successful authorization get settled?

Was every settlement recorded?

Did any duplicate payments occur?

This process is called reconciliation.

It ensures financial accuracy.

---

# Complete End-to-End Flow

```text
Customer

↓

Merchant Application

↓

API Gateway

↓

Authentication

↓

Payment Service

↓

Fraud Detection

↓

Payment Router

↓

Payment Processor

↓

Card Network

↓

Issuing Bank

↓

Authorization

↓

Payment Processor

↓

Gateway

↓

Payment Service

↓

Kafka

├── Notification Service

├── Audit Service

├── Analytics Service

└── Settlement Service

↓

PostgreSQL

↓

Redis
```

---

# Where Can Payments Fail?

Payments can fail at many stages.

Examples:

Merchant Authentication

❌ Invalid API Key

Payment Validation

❌ Invalid Amount

Fraud Detection

❌ Suspicious Transaction

Processor

❌ Timeout

Card Network

❌ Unavailable

Issuing Bank

❌ Insufficient Funds

OTP Validation

❌ Incorrect OTP

Settlement

❌ Banking Delay

A robust payment gateway must gracefully handle failures at every stage.

---

# Why Distributed Systems Are Necessary

Imagine processing:

```
10 million payments/day
```

One server cannot handle such traffic.

Modern payment gateways therefore use:

- Multiple API instances
- Load balancers
- Distributed databases
- Message queues
- Event streaming
- Distributed caches
- Auto scaling
- Fault tolerance

These architectural decisions enable high availability and scalability.

---

# Mapping This Flow to Our Project

Throughout this project, we will implement each stage of the payment lifecycle.

| Phase | Component |
|--------|-----------|
| Order Creation | Merchant Application |
| Payment Request | API Gateway |
| Validation | Payment Service |
| Fraud Checks | Fraud Service |
| Routing | Payment Router |
| Authorization | Processor Simulator |
| Event Publishing | Kafka |
| Persistence | PostgreSQL |
| Caching | Redis |
| Notifications | Notification Service |
| Settlement | Settlement Service |
| Monitoring | Prometheus & Grafana |

By the end of the project, every box in this table will correspond to real code that you build.

---

# Key Takeaways

- Online payments involve multiple independent participants.
- Authorization reserves money; capture collects it; settlement transfers it.
- Payment records should be created before calling external systems.
- Kafka enables asynchronous event-driven communication.
- Audit logs and reconciliation are essential for financial systems.
- Distributed architecture improves scalability, reliability, and fault tolerance.
- Every payment goes through a well-defined lifecycle from order creation to settlement.

---

# What's Next?

In the next chapter, we will study the **Payment Ecosystem** in greater depth and understand the responsibilities of each participant, including merchants, customers, payment gateways, payment processors, acquiring banks, issuing banks, card networks, and settlement systems. This foundation is essential before we begin designing individual services in our payment gateway.