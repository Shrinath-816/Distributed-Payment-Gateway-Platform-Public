# Platform / Common Security

## Purpose

`platform/common-security` provides the shared security foundation for
every microservice in the Distributed Payment Gateway Platform.

Instead of each service implementing authentication and authorization
independently, this module centralizes reusable security components and
policies.

------------------------------------------------------------------------

## Why this module exists

Security must be consistent across the entire platform. Duplicating JWT
validation, authentication filters, or security utilities in every
service leads to inconsistent behavior and maintenance overhead.

`common-security` establishes a single, reusable security layer that
every service can depend on.

------------------------------------------------------------------------

## What belongs here

This module contains reusable security infrastructure, such as:

-   JWT parsing and validation
-   Authentication and authorization utilities
-   Security filters
-   Shared security configuration
-   Custom authentication objects
-   Common security constants
-   Cryptographic helper utilities
-   Shared security exceptions

------------------------------------------------------------------------

## What should NOT belong here

Business-specific authorization rules or service logic should never be
placed inside this module.

Examples: - Merchant onboarding logic - Payment orchestration -
Settlement processing - Token vault business logic - Controllers and
repositories

------------------------------------------------------------------------

## Dependency Rule

Every service can depend on `common-security`.

`common-security` must never depend on any business service.

------------------------------------------------------------------------

## Benefits

-   Consistent security across all services
-   Reusable authentication infrastructure
-   Reduced duplication
-   Centralized maintenance
-   Easier auditing and compliance
-   Faster development of new services

------------------------------------------------------------------------

## In this project

Typical classes include:

-   `JwtTokenProvider`
-   `JwtAuthenticationFilter`
-   `SecurityConfiguration`
-   `SecurityConstants`
-   `AuthenticationPrincipal`
-   `JwtClaims`

These components provide the shared security foundation for the
platform.
