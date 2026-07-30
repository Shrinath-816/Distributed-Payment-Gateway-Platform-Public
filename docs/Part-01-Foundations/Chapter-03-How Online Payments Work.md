# Chapter 03 — How Online Payments Work

## Learning Objectives

By the end of this chapter, you will understand:

- How an online payment travels from a customer to a merchant
- Who participates in a payment transaction
- Why payment gateways exist
- What happens after clicking **Pay**
- Why payments are sometimes successful, failed, or pending
- How payment systems ensure security
- Why enterprise payment gateways are highly distributed systems

---

# Introduction

Every day millions of people purchase products online.

- Ordering food
- Booking flights
- Paying electricity bills
- Purchasing courses
- Shopping on Amazon
- Booking Uber rides

Although the payment completes in only a few seconds, an enormous number of systems communicate behind the scenes.

A single payment may travel through:

- Customer
- Browser
- Merchant website
- Payment Gateway
- Payment Processor
- Card Network
- Issuing Bank
- Acquiring Bank
- Fraud Detection Systems
- Notification Systems

The entire journey usually finishes in **2–5 seconds**.

This chapter explains every step.

---

# A Real World Example

Imagine you are buying a laptop online.

Price:

₹80,000

You click

**Pay Now**

Within seconds you receive

> Payment Successful

How did this happen?

Let's follow the complete journey.

---

# Step 1 — Customer Places an Order

The customer selects

- Product
- Quantity
- Shipping Address

The merchant creates an order.

Example:

```
Order ID:
ORD-100234

Amount:
₹80,000

Status:
CREATED
```

Notice something.

No money has moved yet.

Only an order exists.

---

# Step 2 — Customer Clicks Pay

The website sends a request to the Payment Gateway.

Example:

```
POST /payments

{
   OrderId
   Amount
   Currency
   Customer
}
```

The payment gateway now becomes responsible for handling the payment.

---

# Why Doesn't the Merchant Directly Talk to Banks?

Imagine every merchant had to integrate with:

- Visa
- Mastercard
- RuPay
- American Express
- Every Indian Bank
- Every International Bank
- Every Wallet
- Every UPI Provider

This would be impossible.

Instead,

everyone integrates with a Payment Gateway.

The gateway handles everything else.

---

# Role of a Payment Gateway

Think of the payment gateway as an intelligent traffic controller.

Instead of every merchant learning hundreds of banking protocols,

the gateway acts as the middle layer.

Responsibilities include:

- Authentication
- Validation
- Fraud Detection
- Payment Routing
- Retry Logic
- Security
- Logging
- Notifications

Without a payment gateway,

online commerce would become extremely difficult.

---

# Step 3 — Customer Enters Payment Details

The customer chooses a payment method.

Examples:

- Credit Card
- Debit Card
- UPI
- Wallet
- Net Banking
- EMI

Each payment method follows a slightly different flow,

but the overall architecture remains similar.

---

# Example: Card Payment

Customer enters

```
Card Number

Expiry

CVV

Card Holder Name
```

The payment gateway securely receives this information.

Important:

Sensitive card information must never be stored carelessly.

Enterprise systems usually tokenize or encrypt card details.

---

# Step 4 — Payment Gateway Validates Request

Before contacting any bank,

the gateway validates:

- Amount
- Currency
- Merchant
- Customer
- Duplicate Requests
- Fraud Rules
- Card Format

Example validations:

✓ Amount > 0

✓ Merchant Active

✓ Currency Supported

✓ Card Format Correct

If validation fails,

the payment stops immediately.

---

# Step 5 — Payment Gateway Selects Payment Processor

Large gateways usually connect with multiple processors.

Example:

Processor A

Processor B

Processor C

Why?

Suppose Processor A is down.

Instead of failing,

the gateway routes the payment to Processor B.

This improves reliability.

---

# Step 6 — Payment Processor Contacts Card Network

The processor communicates with

- Visa
- Mastercard
- RuPay
- American Express

These organizations are called

Card Networks.

They do not hold your money.

Instead,

they know which bank issued your card.

---

# Step 7 — Card Network Finds Issuing Bank

Every card belongs to an issuing bank.

Example:

Your card belongs to

HDFC Bank.

The card network forwards the request to HDFC.

---

# Step 8 — Issuing Bank Verifies the Payment

The issuing bank performs multiple checks.

Examples:

Is the card valid?

Has the card expired?

Is CVV correct?

Is OTP correct?

Does the customer have enough balance?

Has the card been blocked?

Has the daily transaction limit been exceeded?

If every validation passes,

the bank authorizes the payment.

---

# Authorization

Authorization does NOT mean money has moved.

It simply means

"I promise this money is available."

Think of it like reserving a hotel room.

The room is reserved.

Nobody else can book it.

Similarly,

the money becomes reserved.

---

# Step 9 — Authorization Response

The issuing bank returns one of the following.

Approved

or

Declined

Example:

```
APPROVED

Authorization Code

TXN12345
```

or

```
DECLINED

Reason:

Insufficient Balance
```

---

# Step 10 — Response Travels Back

The response returns in reverse order.

Issuing Bank

↓

Card Network

↓

Payment Processor

↓

Payment Gateway

↓

Merchant

↓

Customer

This entire round trip usually takes only a few seconds.

---

# Customer Sees the Result

If approved

```
Payment Successful
```

If declined

```
Payment Failed
```

---

# What Happens to the Money?

This surprises many beginners.

Money is NOT immediately transferred.

The payment only gets authorized.

Actual settlement happens later.

Usually:

T+1

or

T+2

depending on the processor and bank.

---

# Authorization vs Capture

These are different operations.

Authorization

Means

Reserve the money.

Capture

Means

Actually collect the money.

Many businesses authorize first,

then capture later.

Example:

Hotel Booking

Airline Booking

Car Rental

---

# Settlement

Settlement is the process of transferring money.

Flow:

Customer Bank

↓

Acquiring Bank

↓

Merchant Account

Only after settlement does the merchant actually receive the funds.

---

# Refund Flow

Refunds work differently.

Merchant

↓

Gateway

↓

Processor

↓

Card Network

↓

Issuing Bank

↓

Customer

Refunds may take several days because they follow banking settlement cycles.

---

# Why Payments Sometimes Stay Pending

Sometimes you see

```
Payment Pending
```

Possible reasons:

- Bank Timeout
- Network Failure
- Processor Busy
- Gateway Retry
- Bank Maintenance
- Internet Issues

The system waits for confirmation before deciding whether the payment succeeded or failed.

---

# Why Duplicate Payments Happen

Imagine this situation.

Customer clicks

Pay

Nothing happens.

Customer clicks again.

Without protection,

two payments may occur.

Enterprise gateways prevent this using

Idempotency.

Each payment request receives a unique identifier.

Repeated requests with the same identifier return the previous result instead of creating another payment.

---

# Why Payment Gateways Need Retry Logic

Sometimes failures are temporary.

Example:

Processor timeout

Instead of failing immediately,

the gateway retries.

This significantly improves payment success rates.

---

# Why Security Is Extremely Important

Payment gateways process sensitive financial information.

Therefore they implement:

- Encryption
- Tokenization
- HTTPS
- Authentication
- Authorization
- Fraud Detection
- Rate Limiting
- Audit Logs
- PCI DSS Compliance

Security is never optional.

It is the foundation of the entire payment ecosystem.

---

# Complete Payment Flow

```
Customer
    │
    ▼
Merchant Website
    │
    ▼
Payment Gateway
    │
    ▼
Payment Processor
    │
    ▼
Card Network
    │
    ▼
Issuing Bank
    │
 Authorization
    │
    ▲
Card Network
    ▲
Payment Processor
    ▲
Payment Gateway
    ▲
Merchant
    ▲
Customer
```

Later

```
Settlement

Customer Bank

↓

Acquiring Bank

↓

Merchant Bank Account
```

---

# Why Payment Gateways Are Distributed Systems

A payment gateway cannot depend on a single server.

If one server crashes,

payments must continue.

Therefore enterprise gateways use:

- Multiple Services
- Load Balancers
- Message Queues
- Databases
- Distributed Caches
- Multiple Processors
- Retry Systems
- Event Streaming
- Monitoring
- High Availability

This is why payment gateways are excellent examples of distributed system design.

---

# How This Relates to Our Project

Throughout this book, we will build a simplified enterprise payment gateway inspired by real-world systems like Stripe and Razorpay.

Our project will include:

- Merchant APIs
- Payment APIs
- Gateway Service
- Routing Engine
- Fraud Detection
- Kafka-based Event Processing
- PostgreSQL
- Redis
- Authentication
- Notifications
- Distributed Transactions
- Monitoring
- Logging
- Containerized Deployment using Docker

Every component we build in later chapters maps directly to the concepts introduced in this chapter.

---

# Key Takeaways

- A payment is much more than moving money.
- Multiple organizations participate in every transaction.
- Payment gateways simplify merchant integrations.
- Authorization and settlement are different processes.
- Payment processors communicate with card networks and banks.
- Reliability, retries, and idempotency are essential.
- Security is mandatory in every payment system.
- Modern payment gateways are large-scale distributed systems built for high availability, fault tolerance, and scalability.

---

# What's Next?

In the next chapter, we will understand the major participants of the payment ecosystem in much greater detail, including:

- Customer
- Merchant
- Payment Gateway
- Payment Processor
- Acquiring Bank
- Issuing Bank
- Card Networks
- UPI PSPs
- Wallet Providers
- Settlement Systems

Understanding these roles is essential before diving into the architecture of our enterprise payment gateway.