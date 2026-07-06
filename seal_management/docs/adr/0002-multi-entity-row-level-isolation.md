# Multi-legal-entity data isolation via row-level legal_entity_id

**Context.** The system serves one **集团** containing many **法人实体** (集团本部 / 子公司 / 分公司). Each legal entity's data — seals, applications, users, etc. — must be isolated from siblings: visible only within its own entity, plus cross-entity read access for **集团** oversight roles (审计员/审批人/管理员).

**Decision.** Single shared PostgreSQL database, shared schema. Every business table carries a `legal_entity_id` column. Isolation is enforced at the data-access layer via MyBatis-Plus's multi-tenant plugin (`TenantLineInnerInterceptor`), which auto-injects the `legal_entity_id` filter into queries. **集团**-level oversight roles use an explicit, tightly-scoped bypass for cross-entity audit/statistics.

**Why.** The legal entities all belong to one trusted **集团** under unified oversight, so the isolation is a business/compliance boundary, not an adversarial one — row-level isolation suffices. It is the standard, lowest-cost SaaS pattern, and enforcing it at the app layer (not via DB-specific RLS) preserves portability (see ADR-0001).

## Considered options

- Schema-per-entity (one PG schema per legal entity) — stronger isolation, but heavy DDL/operational complexity across potentially dozens of entities.
- DB-per-entity — strongest isolation, heaviest ops overhead; unjustified for entities under one group.

## Consequences

- Every business table MUST include `legal_entity_id`; a new table that omits it will be silently unfiltered by the tenant plugin — guard against this in review/tests.
- The 集团 oversight "ignore tenant" path must be narrowly limited to audit/statistics roles and audited.
- `legal_entity_id` participates in unique constraints/indexes where scope is per-entity (e.g., seal numbering, department codes).
