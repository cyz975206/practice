# Distributed deployment topology

**Context.** The system must support **distributed deployment**: multiple stateless backend instances behind a load balancer, sharing one PostgreSQL, Redis, RabbitMQ, MinIO, and an XXL-JOB executor cluster. State cannot live in a single JVM. Three integration points have non-obvious cluster gotchas: real-time WebSocket push, the Flowable workflow engine, and concurrent seal-number allocation.

**Decision.** Establish a cluster-correct spine and resolve the three gotchas:

1. **Stateless + shared state** — every request reconstructs its context from JWT (the `LegalEntityContext`); sessions, locks, and rate-limit counters live in Redis; file blobs in MinIO; the source of truth in PostgreSQL. No JVM-local state that must be consistent across instances.

2. **All locks are cluster-level** — any mutual exclusion (seal-number allocation, 授权码 uniqueness, dedup) uses a **database** lock (`SELECT ... FOR UPDATE`) or a **Redis** distributed lock (Redisson), never a JVM-local `synchronized` / `ReentrantLock`. (See ADR-0008 for numbering.)

3. **WebSocket 站内信 backplane = RabbitMQ STOMP relay** — Spring's STOMP broker relay routes a notification to the user's session regardless of which instance it is connected to (the producing consumer may be on another node, or be an XXL-JOB task with no instance affinity). Requires the RabbitMQ STOMP plugin.

4. **Flowable runs cluster-native** — process definitions are deployed **once** to the shared DB (no per-startup classpath auto-deploy, which causes version storms across nodes); the async job executor runs on **all** nodes and relies on Flowable's native DB-level job locking to prevent double execution.

**Why.** Each gotcha silently breaks under >1 instance if ignored: a WebSocket push from node B never reaches a user on node A; per-node Flowable auto-deploy pollutes the process-definition version space; a JVM-local lock gives no cross-instance exclusion. The chosen answers are each the standard, cluster-correct solution and stay coherent with the existing stack (RabbitMQ-centric notifications; Flowable's shared global tables per ADR-0003; PostgreSQL as synchronizer per ADR-0008).

## Considered options

- **WebSocket backplane**: Redis pub/sub (lighter, but fire-and-forget — acceptable only because the DB is the source of truth for 站内信); sticky sessions (rejected — async / XXL-JOB producers have no instance affinity to the user).
- **Flowable cluster**: per-node classpath auto-deploy (deployment storms); single dedicated Flowable instance (single point of failure + RPC boundary, breaks horizontal scale).
- **Locking**: JVM-local locks (rejected — not cluster-safe).

## Consequences

- RabbitMQ requires the STOMP plugin enabled in every environment.
- Flowable async-executor pool sizing must be tuned (per-node pool × node count = total concurrent jobs); monitor DB job-lock contention.
- A deployment step deploys BPMN process definitions explicitly (do not rely on Spring Boot auto-deploy), tied to the versioning rule in ADR-0003 (in-flight applications keep their start-time version).
- Every new feature that needs mutual exclusion must choose a cluster-level lock — review-guarded (a JVM-local lock is a latent distributed bug).
