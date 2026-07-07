# Seal numbering: registry table + DB row lock + lowest-available gap-fill

**Context.** Every **印章** receives a **印章编号** generated per **法人实体** by that entity's **编号规则**. Its **流水号** segment must be: auto-allocated to the *lowest available* number; user-modifiable; subject to **占号** (reserve a specific number, in-between numbers stay available) and **跳号** (permanently exclude a number); concurrency-safe; and the system runs under **distributed deployment** (multiple stateless instances sharing one PostgreSQL).

**Decision.** Store every consumed serial in a per-entity registry table `seal_number_registry(legal_entity_id, serial, kind)` where `kind ∈ {used, reserved(占号), skipped(跳号)}` — the single source of truth. Allocate under a **database-level pessimistic row lock** on a per-entity allocator row (`SELECT ... FOR UPDATE` on `seal_number_allocator WHERE legal_entity_id = ?`), then compute the lowest positive integer absent from the registry (fill existing gaps first, else high-water + 1), insert the registry row, bump the allocator, and commit. A `UNIQUE(legal_entity_id, serial)` constraint is the final collision guard. **占号 / 跳号** are ordinary inserts with the matching `kind`. No JVM-local locks; no DB sequences; no Redis for allocation state.

**Why.** "Lowest available" requires filling gaps, which a DB `SEQUENCE` cannot do (sequences only go up). The registry + absent-minimum query expresses gap-filling, **占号**, and **跳号** uniformly (they are just rows) and keeps numbering state in one place — PostgreSQL, the cluster's shared synchronizer — so a DB row lock is naturally cluster-safe under distributed deployment. This stays pure, portable SQL (ADR-0001's 信创 goal) and MyBatis-Plus-friendly, unlike a PG-specific sequence or a Redis allocator that splits state across stores.

## Considered options

- **PostgreSQL SEQUENCE + reserved rows** — inherent concurrency safety and cluster-safe, but a sequence never re-fills gaps (contradicts lowest-available) and is PG-proprietary, hurting 信创 portability.
- **Redisson distributed lock + Redis INCR / set** — cluster-safe and fast, but splits allocation state across Redis and PostgreSQL (failover consistency risk) and breaks the SQL-portable posture.
- **In-process (JVM) lock** — rejected outright: not cluster-safe under distributed deployment.

## Consequences

- The lowest-available query is a real SQL query (find the minimum absent positive integer per entity); implement and load-test it, and index `(legal_entity_id, serial)`.
- Allocation serializes per entity on one allocator row — acceptable contention for seal-intake volumes (a low rate), not a hot path.
- **占号** of a number already `used` / `skipped` is a conflict — validate and reject with a clear error.
- Renumbering or merging entities is hard (numbers are entity-scoped and may be physically engraved on 实体印章) — treat **印章编号** as immutable once issued.
- Numbering state lives only in PostgreSQL — no cache layer to keep coherent with the registry.
