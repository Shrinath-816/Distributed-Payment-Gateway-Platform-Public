# Platform / Common Test Support

## Purpose

`platform/common-test-support` provides the shared testing foundation
for every microservice in the Distributed Payment Gateway Platform.

Instead of each service creating its own testing utilities and
configuration, this module centralizes reusable test infrastructure.

------------------------------------------------------------------------

## Why this module exists

Enterprise platforms require consistent testing practices. Duplicating
test configuration, helper classes, and common fixtures across services
increases maintenance effort and leads to inconsistent tests.

`common-test-support` provides one reusable testing layer that every
service can depend on.

------------------------------------------------------------------------

## What belongs here

-   Base integration test classes
-   Testcontainers configuration
-   Mock object factories
-   Test data builders
-   Common assertions
-   Shared test utilities
-   Integration test configuration
-   Reusable test fixtures

------------------------------------------------------------------------

## What should NOT belong here

Business-specific tests should never be placed here.

Examples:

-   Merchant service tests
-   Payment orchestration tests
-   Settlement tests
-   Token vault business tests
-   Service-specific controller or repository tests

------------------------------------------------------------------------

## Dependency Rule

Every service can depend on `common-test-support`.

`common-test-support` must never depend on business modules.

------------------------------------------------------------------------

## Benefits

-   Consistent testing across services
-   Reusable test infrastructure
-   Reduced duplication
-   Faster test development
-   Easier maintenance
-   Standardized Testcontainers setup
-   Improved test reliability

------------------------------------------------------------------------

## In this project

Typical classes include:

-   `BaseIntegrationTest`
-   `TestContainersConfiguration`
-   `MockFactory`
-   `TestDataBuilder`
-   `Assertions`
-   `TestUtils`

These components provide a common testing foundation and ensure every
microservice follows the same testing standards.
