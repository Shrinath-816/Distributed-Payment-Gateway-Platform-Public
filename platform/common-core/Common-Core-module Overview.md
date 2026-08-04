# Platform / Common Core

## Purpose

`platform/common-core` is the shared foundation of the entire
Distributed Payment Gateway Platform.

Every service depends on this module for common building blocks instead
of creating its own copies.

------------------------------------------------------------------------

## Why this module exists

Without a shared core, each microservice would duplicate the same
classes such as:

-   Exceptions
-   Error codes
-   DTOs
-   Constants
-   Enums
-   Utility classes

This leads to inconsistent behavior, duplicated code, and higher
maintenance cost.

`common-core` provides a **single source of truth** that every service
can reuse.

------------------------------------------------------------------------

## What belongs here

This module contains only framework-neutral, reusable components, for
example:

-   Shared constants (headers, scopes, etc.)
-   Common DTOs
-   Base exception hierarchy
-   Error codes
-   Common enums
-   Utility classes
-   Generic interfaces and contracts

------------------------------------------------------------------------

## What should NOT belong here

Business logic must never be placed inside `common-core`.

Examples of classes that do **not** belong here:

-   PaymentService
-   MerchantService
-   TokenizationService
-   SettlementCalculator
-   Repository classes
-   Controller classes

These belong to their respective microservices.

------------------------------------------------------------------------

## Dependency Rule

Every service **can depend on** `common-core`.

`common-core` should **not depend on any business module**.

This keeps the architecture clean, reusable, and free from circular
dependencies.

------------------------------------------------------------------------

## Benefits

-   Single source of truth
-   Consistent behavior across all services
-   Reduced code duplication
-   Easier maintenance
-   Cleaner architecture
-   Better scalability
-   Faster development of new services

------------------------------------------------------------------------

## In this project

Examples of files located here include:

-   `HeaderNames`
-   `ScopeConstants`
-   `ErrorResponse`
-   `BaseException`
-   `ErrorCode`

These classes are shared by multiple services and form the foundational
layer of the platform.
