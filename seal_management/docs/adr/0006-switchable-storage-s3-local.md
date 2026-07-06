# Storage abstraction: switchable S3 / local adapters

**Context.** File storage (印模, 待用印 documents, scanned stamped evidence, signed PDFs, seal-archive photos) must not be hard-wired to one provider. Local development benefits from a zero-dependency local filesystem; production and 信创 deployments need S3-compatible object storage (MinIO / AWS S3 / 阿里云 OSS S3-compatible / domestic object stores). The provider differs per environment.

**Decision.** Define a `StoragePort` (upload / download / presigned URL / delete / exists) and provide two adapters, selected by configuration (`storage.type`):
- **S3 adapter** — speaks the S3 API; compatible with MinIO, AWS S3, 阿里云 OSS (S3-compatible), and 信创 object stores.
- **Local adapter** — writes to the local filesystem (for dev / no-S3 environments).

Spring selects the active adapter via `@ConditionalOnProperty` (or a factory), so business code depends only on `StoragePort`.

**Why.** The provider must be switchable per environment without touching business code — consistent with ports-and-adapters (ADR-0004) and the 信创 portability goal (ADR-0001). The local adapter removes the MinIO dependency for quick local dev; the S3 adapter covers production and 信创 with a single implementation (S3 is the de-facto interop protocol).

## Considered options

- Hardcode MinIO only — simplest, but forces every environment (incl. 信创 with a different object store) onto MinIO.
- Vendor-specific SDKs scattered in business code — couples the domain to the vendor and is untestable.

## Consequences

- The Port contract must stay provider-neutral: presigned URLs (S3) vs signed local paths differ — return a generic handle (e.g., a `StorageResource` / access URL abstraction), not S3-specific types.
- The local adapter cannot fully simulate S3 semantics (no real presigned URLs, no per-object ACL) — restrict or simulate those features in local mode and document the gaps.
- Object metadata (mime, size, hash, original filename) is stored in PostgreSQL regardless of provider; the blob is the only provider-specific part.
