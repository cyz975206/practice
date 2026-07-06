# Technology stack and 信创 portability

**Context.** The system targets large 央企/国企 groups (multi-legal-entity) where 信创 / domestic-stack deployment is often eventually required. It is also a practice project, so local developer experience and documentation matter.

**Decision.** Backend on **Spring Boot 3 + Java 17, PostgreSQL, MyBatis-Plus, Redis, REST + JWT**. Frontends: `vue` and `react` as two parallel PC implementations of the same system, `h5` as the mobile frontend, all consuming one framework-agnostic API.

**Why these choices (the non-obvious part).** We run the mainstream stack now for developer experience, but deliberately keep **SQL standard and vendor-agnostic** so a future migration to 信创 databases is feasible.
- **PostgreSQL over MySQL** — the most likely 信创 targets (OpenGauss, 人大金仓) are PostgreSQL-derived, so the migration gap is smallest.
- **MyBatis-Plus over JPA/Hibernate** — transparent, hand-visible SQL is easier to keep portable than ORM-generated SQL.

## Considered options

- MySQL + MyBatis-Plus — more tutorials, but a larger gap to the PG-based 信创 targets.
- JPA/Hibernate — faster development, but opaque generated SQL hurts portability control.

## Consequences

- Avoid PostgreSQL-specific proprietary features where a standard alternative exists (prefer standard SQL types and syntax). Keep dialect differences isolated.
- Accept the cost of slightly more verbose data-access code (MyBatis-Plus mappers) in exchange for portability and SQL transparency.
