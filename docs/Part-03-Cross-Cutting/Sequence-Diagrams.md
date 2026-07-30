# Sequence-Diagrams.md

---

## Payment Flow

```mermaid
sequenceDiagram
    autonumber
    participant Client
    participant GW as API Gateway
    participant POS as Payment Orchestrator
    participant MS as Merchant Service
    participant TV as Token Vault
    participant AA as Acquiring Adapter
    participant SS as Settlement Service
    participant WH as Webhook Service

    Client->>GW: POST /v1/payments
    GW->>POS: forward (mTLS)
    POS->>MS: eligibility check
    POS->>TV: detokenize
    POS->>AA: authorize
    AA-->>POS: result
    POS-->>Client: response (via GW)
    POS->>SS: (async) ledger.events
    POS->>WH: (async) payment.events
```

---

## Payment Retry

```mermaid
flowchart TD
    A["Step fails"] --> B{"Business decline?"}
    B -->|Yes| C["Terminal — no retry"]
    B -->|No, transient| D{"Attempts < max?"}
    D -->|Yes| E["Backoff + retry"]
    E --> A
    D -->|No| F{"Prior step already effective?"}
    F -->|Yes| G["Trigger Compensation"]
    F -->|No| H["Mark FAILED"]
```

---

## Authorization

```mermaid
sequenceDiagram
    autonumber
    participant POS as Payment Orchestrator
    participant TV as Token Vault
    participant AA as Acquiring Adapter

    POS->>TV: detokenize(vaultToken)
    TV-->>POS: PAN reference (<50ms)
    POS->>AA: authorize(amount, PAN reference)
    AA-->>POS: APPROVED/DECLINED
    POS->>POS: discard PAN reference
```

---

## Capture

```mermaid
sequenceDiagram
    autonumber
    participant Merchant
    participant POS as Payment Orchestrator
    participant AA as Acquiring Adapter

    Merchant->>POS: POST /v1/payments/{id}/capture
    POS->>POS: verify state == AUTHORIZED
    POS->>AA: capture(amount)
    AA-->>POS: SUCCESS
    POS->>POS: ledger entry + state → CAPTURED
    POS-->>Merchant: 200
```

---

## Refund

```mermaid
sequenceDiagram
    autonumber
    participant Merchant
    participant POS as Payment Orchestrator
    participant AA as Acquiring Adapter
    participant SS as Settlement Service

    Merchant->>POS: POST /v1/payments/{id}/refunds
    POS->>POS: verify state == CAPTURED
    POS->>AA: refund(amount)
    AA-->>POS: SUCCESS
    POS->>POS: ledger entry + state → REFUND_PARTIAL/FULL
    POS->>SS: (async) ledger.events
```

---

## Settlement

```mermaid
sequenceDiagram
    autonumber
    participant Sched as Schedule Manager
    participant Batch as Batch Processor
    participant Fee as Fee Calculator
    participant Payout as Payout Generator
    participant MS as Merchant Service
    participant Bank as Banking System

    Sched->>Batch: cutoff reached, create batch
    Batch->>Fee: calculate fees/net amount
    Fee-->>Batch: net amount
    Batch->>MS: lookup payout account
    MS-->>Batch: payout account reference
    Batch->>Payout: generate payout instruction
    Payout->>Bank: submit
    Bank-->>Payout: confirmation
```

---

## Webhook

```mermaid
sequenceDiagram
    autonumber
    participant Kafka
    participant WHS as Webhook Service
    participant MS as Merchant Service
    participant Merchant as Merchant Endpoint

    Kafka->>WHS: consume event
    WHS->>MS: lookup webhook config
    MS-->>WHS: {endpointUrl, secret}
    WHS->>WHS: sign payload (HMAC-SHA256)
    WHS->>Merchant: POST signed payload
    alt 2xx
        Merchant-->>WHS: acknowledgement
    else failure
        Merchant-->>WHS: non-2xx/timeout
        WHS->>WHS: retry (up to 7x, backoff)
    end
```

---

## Tokenization

```mermaid
sequenceDiagram
    autonumber
    participant Browser
    participant SDK as Browser SDK
    participant TV as Token Vault

    Browser->>SDK: enter card details
    SDK->>SDK: client-side validation
    SDK->>TV: POST /v1/tokens [direct TLS]
    TV->>TV: encrypt + persist
    TV-->>SDK: {vaultToken, maskedPan}
    SDK-->>Browser: tokenizeSuccess
```

---

## Merchant Onboarding

```mermaid
sequenceDiagram
    autonumber
    participant Merchant
    participant MS as Merchant Service
    participant Auto as Automated Rule Engine
    participant Reviewer as Compliance Reviewer

    Merchant->>MS: register
    MS->>MS: state → PENDING_VERIFICATION
    Merchant->>MS: submit KYC documents
    MS->>MS: state → UNDER_REVIEW
    MS->>Auto: evaluate
    alt auto-approved
        Auto-->>MS: APPROVED
    else manual review
        MS->>Reviewer: enqueue
        Reviewer-->>MS: decision
    end
    alt APPROVED
        MS->>MS: state → ACTIVE
    else REJECTED
        MS->>MS: state → PENDING_VERIFICATION
    end
```

---

## Saga Flow

```mermaid
sequenceDiagram
    autonumber
    participant SC as Saga Coordinator
    participant TV as Token Vault
    participant AA as Acquiring Adapter
    participant CM as Compensation Manager

    SC->>TV: detokenize
    TV-->>SC: PAN reference
    SC->>AA: authorize
    AA-->>SC: APPROVED
    SC->>AA: capture
    alt capture fails after retries
        AA-->>SC: FAILURE
        SC->>CM: compensate
        CM->>AA: reversal
        CM->>SC: ledger corrected
    else capture succeeds
        AA-->>SC: SUCCESS
        SC->>SC: state → CAPTURED
    end
```

---

## Failure Recovery

```mermaid
flowchart TD
    A["Region/dependency outage detected"] --> B["Promote standby / restore dependency"]
    B --> C["Resume Kafka consumption from last committed offset"]
    C --> D{"Any ambiguous in-flight operations?"}
    D -->|Yes| E["Reconcile via status check<br/>(acquirer / banking system / saga_execution)"]
    D -->|No| F["Resume normal operation"]
    E --> F
```

---

## Service Startup

```mermaid
flowchart TB
    A["Process starts"] --> B["Load configuration"]
    B --> C["Establish dependency connectivity<br/>(DB, Redis, Kafka, HSM/KMS where applicable)"]
    C --> D["Verify schema (Flyway)"]
    D --> E["Register health endpoints"]
    E --> F["Report READY"]
```

---

## Service Shutdown

```mermaid
sequenceDiagram
    autonumber
    participant K8s as Kubernetes
    participant Pod

    K8s->>Pod: SIGTERM
    Pod->>Pod: readiness → false
    Pod->>Pod: stop accepting new requests
    Pod->>Pod: drain in-flight requests (bounded grace period)
    Pod->>Pod: close DB/Redis/Kafka/HSM-KMS connections
    Pod->>K8s: process exits
```