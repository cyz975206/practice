# Device & vendor integrations via ports-and-adapters, mocked for practice

**Context.** The system integrates with three externals that need real hardware or vendor access: **打印用印一体机** (print-seal machine — auto-stamp), **智能印章柜** (smart seal cabinet — storage/access via **授权码**), and **签章接口** (enterprise e-sign API). None is available in the practice/development environment.

**Decision.** Each integration is abstracted behind a **port** (adapter interface) in the domain/application layer — e.g., a print-seal port, a seal-cabinet port, an e-sign port — following the ports-and-adapters (hexagonal) pattern. The practice build ships **Mock** adapter implementations; real adapters (vendor SDK / device protocol) plug in later without touching business logic. A configuration switch selects the active adapter per integration.

**Why.** Business logic must not depend on vendor SDKs or device protocols — they are unstable, environment-specific, and unavailable during practice. The port boundary keeps the domain unit-testable (via Mocks) and lets real hardware/vendors slot in later. The Mocks double as living documentation of each integration's contract.

## Considered options

- Real integration everywhere — infeasible without hardware/vendor access; blocks the practice build.
- Interface-only (ports defined, no implementation) — under-delivers; no runnable demo.
- Scatter vendor calls through business code — couples the domain to vendors and makes it untestable.

## Consequences

- Define each port's contract carefully (idempotency, async results, failure modes) — the Mock must faithfully simulate real device behavior (cabinet open/return timing, e-sign async polling/callbacks).
- Device interactions are often asynchronous — the **用印** fulfillment flow must handle pending / in-progress / confirmed states, not assume synchronous success.
