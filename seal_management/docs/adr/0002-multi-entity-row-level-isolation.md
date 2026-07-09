# Multi-legal-entity data isolation via row-level legal_entity_id

**Context.** The system serves one **集团** containing many **法人实体** (集团本部 / 子公司 / 分公司). Each legal entity's data — seals, applications, users, etc. — must be isolated from siblings: visible only within its own entity, plus cross-entity read access for **集团** oversight roles (审计员/审批人/管理员).

**Decision.** Single shared PostgreSQL database, shared schema. Every business table carries a `legal_entity_id` column. Isolation is enforced at the data-access layer via MyBatis-Plus's multi-tenant plugin (`TenantLineInnerInterceptor`), which auto-injects the `legal_entity_id` filter into queries. **集团**-level oversight roles use an explicit, tightly-scoped bypass for cross-entity audit/statistics.

**Why.** The legal entities all belong to one trusted **集团** under unified oversight, so the isolation is a business/compliance boundary, not an adversarial one — row-level isolation suffices. It is the standard, lowest-cost SaaS pattern, and enforcing it at the app layer (not via DB-specific RLS) preserves portability (see ADR-0001).

## Considered options

- Schema-per-entity (one PG schema per legal entity) — stronger isolation, but heavy DDL/operational complexity across potentially dozens of entities.
- DB-per-entity — strongest isolation, heaviest ops overhead; unjustified for entities under one group.

## Consequences

- Every business table MUST include `legal_entity_id`; a new table that omits it will be silently unfiltered by the tenant plugin — guard against this in review/tests.
- The cross-**法人实体** ("ignore tenant") path is gated by a **GROUP-scoped role** (scope is a property of the role, not of the user's home entity): only **集团审计员 / 集团管理员** (audit/statistics) may use it, and every such access is itself audited.
- `legal_entity_id` participates in unique constraints/indexes where scope is per-entity (e.g., seal numbering, department codes).
- **Role model (as built):** roles are stored **per-legal-entity** (`sys_role` is itself a tenant table) — not a fixed global catalog; each entity manages its own roles. On every startup (idempotent, re-runnable) the system ensures three system roles (all scope `ENTITY`) in the default legal entity (集团本部): **超级管理员** `super_admin` (deployment owner; the only role permitted to manage 法人实体; does **not** bypass tenant isolation — cross-entity GROUP bypass stays deferred), **系统管理员** `admin` (= 法人实体管理员), and **普通用户** `user` (= 申请人; auto-granted to API-created users as the default role). The bootstrap `admin`/`888888` user holds `super_admin`. Other roles are admin-created custom roles, inert for system authorization until a role→resource permission table lands. **The super-admin does NOT bypass tenant isolation** — it is the most-privileged role *within its entity*, still bound to `legal_entity_id`. The CONTEXT's **GROUP-scoped** 集团-level roles (集团审计员/管理员/审批人) and their cross-entity bypass are **deferred** (not yet implemented). Login uses a globally-unique `username`, looked up via an `@InterceptorIgnore(tenantLine)` mapper method (login happens before the tenant context is known).
