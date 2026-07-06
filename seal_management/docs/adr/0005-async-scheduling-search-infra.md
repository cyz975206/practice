# Async, scheduling & search infrastructure: RabbitMQ + XXL-JOB + Elasticsearch

**Context.** The core stack (Spring Boot + PostgreSQL + Redis + Flowable) does not cover three real needs: (a) async/decoupled messaging — notification dispatch, async audit-log persistence, device-event handling, post-fulfillment side-effects; (b) time-based tasks — borrowing-timeout reminders, duty-roster (排班) rotation, scheduled reports, data archival/cleanup; (c) large-volume full-text search and aggregation over audit logs and usage records at **集团** scale, which PostgreSQL handles poorly.

**Decision.** Add three infrastructure components:
- **RabbitMQ** — async messaging: notification dispatch, audit-log async write, device events, post-fulfillment side-effects.
- **XXL-JOB** — distributed scheduled tasks: borrowing-timeout reminders, duty-roster rotation, scheduled reports, data archival/cleanup.
- **Elasticsearch** — search & analytics: audit-log and usage-record indexing, cross-entity statistics/reports for **集团** oversight.

**Why these.** Each fills a genuine gap, not keyword-stuffing. RabbitMQ's flexible routing (exchanges/queues) fits the multi-channel notification + event patterns at moderate throughput. XXL-JOB is the CN-enterprise-standard distributed scheduler with a central admin UI (the repo already has scheduled-task-log features). Elasticsearch handles full-text search and aggregation that PostgreSQL is poor at on large audit volumes.

## Considered options

- **MQ**: Kafka (overkill throughput, log-streaming model), RocketMQ (strong, esp. transactional messages) — RabbitMQ chosen for universal fit, flexible routing, and easy ops.
- **Scheduler**: Quartz / Spring `@Scheduled` (single-node, no admin UI), PowerJob — XXL-JOB chosen for CN-enterprise fit, distributed execution, and admin UI.
- **Search**: PostgreSQL full-text search (weak at scale + aggregation) — Elasticsearch chosen.

## Consequences

- Three more components in the Docker Compose stack (`rabbitmq`, `xxl-job-admin` + executor, `elasticsearch`) — heavier local environment.
- Audit log flows app → RabbitMQ → consumer → PostgreSQL (source of truth) **and** → Elasticsearch (search index); define the consistency model (at-least-once + idempotent consumer; ES sync via the consumer, not dual-write in the transaction).
- Scheduled tasks run on the XXL-JOB executor; borrowing-timeout and roster-rotation logic lives there, publishing reminders through the notification service.
- ES indexing scope still respects **法人实体** isolation (index documents carry `legal_entity_id`; 集团 oversight queries use a privileged search path).
