# Multi-legal-entity data isolation via row-level legal_entity_id

**Context.** The system serves one **集团** containing many **法人实体** (集团本部 / 子公司 / 分公司). Each legal entity's data — seals, applications, users, etc. — must be isolated from siblings: visible only within its own entity, plus cross-entity access for **集团** oversight/admin roles (超级审计员 / 超级管理员).

**Decision.** Single shared PostgreSQL database, shared schema. Every business table carries a `legal_entity_id` column. Isolation is enforced at the data-access layer via MyBatis-Plus's multi-tenant plugin (`TenantLineInnerInterceptor`), which auto-injects the `legal_entity_id` filter into queries. **集团**-level oversight roles use an explicit, tightly-scoped bypass for cross-entity audit/statistics.

**Why.** The legal entities all belong to one trusted **集团** under unified oversight, so the isolation is a business/compliance boundary, not an adversarial one — row-level isolation suffices. It is the standard, lowest-cost SaaS pattern, and enforcing it at the app layer (not via DB-specific RLS) preserves portability (see ADR-0001).

## Considered options

- Schema-per-entity (one PG schema per legal entity) — stronger isolation, but heavy DDL/operational complexity across potentially dozens of entities.
- DB-per-entity — strongest isolation, heaviest ops overhead; unjustified for entities under one group.

## Consequences

- Every business table MUST include `legal_entity_id`; a new table that omits it will be silently unfiltered by the tenant plugin — guard against this in review/tests.
- The cross-**法人实体** ("ignore tenant") path is gated by **GROUP-scoped roles** (scope is a property of the role, not of the user's home entity): **超级管理员** (cross-entity admin) and **超级审计员** (read-only audit/statistics); every such access is itself audited. **This bypass is designed but NOT yet implemented** — currently `super_admin` is treated as ENTITY-scoped and no ignore-tenant path exists (tracked as pending; see `MybatisPlusConfig` TODO).
- `legal_entity_id` participates in unique constraints/indexes where scope is per-entity (e.g., seal numbering, department codes).
- **Role model (designed — 4 fixed system roles):** roles are stored **per-legal-entity** (`sys_role` is itself a tenant table); each entity manages its own custom roles. The system defines **four fixed system roles** in two scopes:
  - **GROUP-scoped (bypass tenant isolation — *designed, bypass NOT yet implemented*):** **超级管理员** `super_admin` (deployment-wide admin; the only role that manages 法人实体, plus cross-entity users/config) and **超级审计员** `super_auditor` (deployment-wide read-only audit/statistics; = the former 集团审计员).
  - **ENTITY-scoped (implemented):** **系统管理员** `admin` (= 法人实体管理员; manages its own entity's users/roles/org) and **普通用户** `user` (= 申请人; auto-granted to API-created users as the default role).
  The bootstrap `admin`/`888888` user holds `super_admin`. Other roles (e.g. 审批人, 集团审批人) are admin-created **custom roles**, inert for system authorization until a role→resource permission table lands.
  > **Current code gap:** only the two ENTITY roles behave as specified today. The two GROUP roles' tenant-bypass is **not yet wired** (the `MybatisPlusConfig` ignore-tenant path is a TODO); until then `super_admin` is effectively ENTITY-scoped. 集团管理员 / 集团审计员 have been renamed to 超级管理员 / 超级审计员 (the system may serve small enterprises, not only a 集团). Login uses a globally-unique `username`, looked up via an `@InterceptorIgnore(tenantLine)` mapper method (login happens before the tenant context is known).
