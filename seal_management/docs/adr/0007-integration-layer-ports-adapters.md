# Integration layer: capability ports + per-enterprise adapters + config registry

**Context.** The system is a product deployed for different enterprises (**集团**), each surrounded by a different ecosystem: **统一平台** (org/personnel master data), **影像平台** (document archive), **调度系统** (dispatch), **日志系统** (centralized logging), **大数据平台** (analytics). Integrations vary per deployment and must be pluggable without bespoke per-customer code in the business core.

**Decision.** Add an **Integration Layer** on top of the existing event bus (RabbitMQ) and scheduler (XXL-JOB). Each external-system category is an **Integration Port** (a capability interface): `MasterDataSyncPort`, `DocumentArchivePort`, `LogShippingPort`, `BigDataSyncPort`, `DispatchPort`. Each port has per-enterprise **Integration Adapters**. A per-deployment **Integration Config Registry** enables capabilities, selects adapters, and holds connection params. Event-driven integrations subscribe to existing MQ events as additional consumers; sync-type integrations run on XXL-JOB; request-type are called inline. Business code depends only on ports; disabled capabilities are no-ops.

**Why.** The same ports-and-adapters principle as ADR-0004, generalized from seal-specific devices to the enterprise ecosystem. Building on the existing MQ + scheduler avoids a separate integration backbone. Per-deployment config makes each enterprise's integrations a *configuration* concern, not a *code-fork* concern — the product stays one codebase.

## Considered options

- Unified integration gateway/bus (ESB-style) — heavier infra, more moving parts than this scope needs.
- Point-to-point hardcoded per customer — high duplication, unmaintainable across enterprises.

## Consequences

- **统一平台** may become the source of truth for org/people master data — define a sync reconciliation strategy (conflict handling, manual overrides, idempotency via stable external IDs).
- Event-driven adapters are extra MQ consumers — isolate topology/DLQs so enterprise-forwarding can never block core consumers.
- Per-adapter retry/backoff/DLQ + circuit-breaking; idempotent receivers (external systems may redeliver).
- Each port's contract is documented as the capability ("what"); enterprise-specific mapping lives in the adapter ("how").
