# Chapter 01 --- What is Software Architecture?

> **Part 01 -- Foundations**

> **Role of this Guide:** This book is written by your **System Design
> Tutor**. Every chapter is designed not only to explain *this payment
> gateway*, but also to teach you the principles that apply to almost
> every large-scale software system.

------------------------------------------------------------------------

# Learning Objectives

By the end of this chapter, you will be able to:

-   Explain what software architecture is in simple words.
-   Understand why architecture is created before coding.
-   Recognize the difference between architecture and implementation.
-   Apply the same thinking to any software project, not just payment
    systems.

------------------------------------------------------------------------

# What is Software Architecture?

Imagine someone asks you to build a 20-floor apartment.

Would you immediately start laying bricks?

Of course not.

First, an architect creates a **blueprint** showing:

-   Rooms
-   Floors
-   Electrical wiring
-   Water pipelines
-   Emergency exits

Only after everyone agrees on the blueprint does construction begin.

Software is built exactly the same way.

**Software Architecture is the blueprint of a software system.**

It describes the major building blocks, their responsibilities, how they
communicate, how data moves, and how the system can grow safely.

------------------------------------------------------------------------

# Simple Definition

> **Software Architecture is the high-level design of a software system
> that defines its structure, responsibilities, communication,
> scalability, security, and reliability.**

Architecture focuses on **how the whole system works together**, not on
individual lines of code.

------------------------------------------------------------------------

# Architecture vs Coding

## Architecture answers

-   What are we building?
-   What components do we need?
-   How will they communicate?
-   Where will data be stored?
-   How will failures be handled?
-   How will the system scale?

## Coding answers

-   How do I implement this API?
-   How do I save data?
-   How do I validate input?
-   How do I call another service?

**Architecture decides. Coding implements.**

------------------------------------------------------------------------

# Real-Life Analogy

Think about a hospital.

-   Doctors treat patients.
-   Nurses provide care.
-   Receptionists register patients.
-   Pharmacists provide medicines.
-   Laboratories perform tests.

No one person performs every job.

Likewise, in good software architecture, every component has one clear
responsibility.

------------------------------------------------------------------------

# Why Every Large System Needs Architecture

Small applications may work with:

-   One frontend
-   One backend
-   One database

As systems grow, they need:

-   More users
-   More developers
-   Better security
-   Faster performance
-   Easier maintenance
-   Independent scaling

Without architecture, complexity quickly becomes unmanageable.

------------------------------------------------------------------------

# How This Relates to Our Project

Our distributed payment gateway contains multiple independent
responsibilities such as:

-   Merchant onboarding
-   Card tokenization
-   Payment processing
-   Webhooks
-   Settlement
-   Event processing
-   Monitoring

If everything were placed in one application, the system would become
difficult to understand, test, secure, and scale.

Throughout this guide, you'll learn **why each responsibility becomes
its own component**.

------------------------------------------------------------------------

# Universal System Design Principle

Before writing code, always ask:

1.  What problem am I solving?
2.  Who are the users?
3.  What are the major responsibilities?
4.  How should responsibilities be separated?
5.  How should components communicate?
6.  What happens if one component fails?
7.  How will the system grow in the future?

These questions apply to **every system**, whether it's an e-commerce
platform, banking application, ride-sharing app, social network, or this
payment gateway.

------------------------------------------------------------------------

# Key Takeaways

-   Architecture is the blueprint of software.
-   Coding comes after architecture.
-   Large systems succeed because responsibilities are divided clearly.
-   Good architecture makes systems easier to build, maintain, secure,
    and scale.
-   The concepts you learn in this guide are transferable to almost
    every distributed system.

------------------------------------------------------------------------

# Looking Ahead

In the next chapter, we'll answer a more important question:

**What exactly is a Payment Gateway, and why does the world need one?**
