# Chapter 02 --- What is a Payment Gateway?

> **Part 01 -- Foundations**

## Learning Objectives

-   Understand what a payment gateway is.
-   Learn why payment gateways exist.
-   Understand the complete payment ecosystem.
-   Learn where our project fits.
-   Build system design thinking beyond this project.

------------------------------------------------------------------------

# Imagine Buying a Laptop Online

You purchase a laptop from an online store.

You click **Pay Now**.

Within seconds:

-   Your card is verified.
-   Your bank checks your account.
-   Money is approved or declined.
-   The website receives the result.
-   Your order is confirmed.

The online store does not perform all these tasks.

A **Payment Gateway** coordinates everything.

------------------------------------------------------------------------

# Definition

> A **Payment Gateway** is a secure software system that accepts payment
> requests from merchants, communicates with banks and payment
> providers, and safely returns the payment result.

Think of it as a **bridge** between the merchant and the financial
world.

------------------------------------------------------------------------

# Who Participates?

## Customer

The person making the payment.

## Merchant

The business selling products or services.

Examples:

-   Amazon
-   Flipkart
-   Swiggy
-   Netflix

## Payment Gateway

Our project.

Responsibilities:

-   Receive payment requests
-   Validate requests
-   Protect sensitive information
-   Contact payment providers
-   Return payment status

## Bank

The institution that manages money.

------------------------------------------------------------------------

# Real-Life Analogy

Imagine sending a parcel internationally.

You give it to a courier company.

The courier coordinates airports, customs and delivery.

Similarly, a payment gateway coordinates communication between merchants
and banks.

------------------------------------------------------------------------

# Why Does a Payment Gateway Exist?

Without a gateway:

Every merchant would have to integrate separately with every bank.

That means:

-   Different APIs
-   Different security rules
-   Different authentication methods
-   More maintenance
-   Higher costs

A payment gateway solves this problem by providing one common
integration point.

------------------------------------------------------------------------

# Simplified Payment Flow

1.  Customer clicks **Pay**.
2.  Merchant sends the payment request.
3.  Payment Gateway validates the request.
4.  Gateway contacts the bank or payment provider.
5.  Bank approves or declines.
6.  Gateway receives the response.
7.  Merchant receives the final payment status.

------------------------------------------------------------------------

# Where Does Our Project Fit?

Our project is the **Payment Gateway**.

Customer

↓

Merchant

↓

**Our Distributed Payment Gateway**

↓

Mock Banks / Simulated Providers

Everything inside the highlighted gateway is what we will design and
build.

------------------------------------------------------------------------

# Responsibilities of Our Gateway

-   Merchant onboarding
-   Card tokenization
-   Payment authorization
-   Capture
-   Refunds
-   Webhooks
-   Settlement
-   Event processing
-   Monitoring

These responsibilities will later be separated into multiple
microservices.

------------------------------------------------------------------------

# System Design Lesson

Whenever you design any software system, always ask:

-   Who are the users?
-   What problem am I solving?
-   What belongs inside my system?
-   What belongs outside my system?
-   Which external systems do I depend on?

These questions apply to every distributed system, not only payment
gateways.

------------------------------------------------------------------------

# Key Takeaways

-   A payment gateway is a secure intermediary.
-   It simplifies communication between merchants and banks.
-   It improves security and scalability.
-   Our project builds this gateway using modern distributed system
    principles.

------------------------------------------------------------------------

# Looking Ahead

Next chapter:

**Chapter-03-How-Online-Payments-Work.md**
