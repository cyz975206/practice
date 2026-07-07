# Approval engine: Flowable + custom resolver glue

**Status.** Supersedes the earlier "custom lightweight engine" choice (reversed after clarifying the project's resume-value goal).

**Context.** The approval model (**审批流** → **审批节点**, each node = resolver × **签批方式**) requires domain-specific resolution: **岗位** with primary/backup (**B角**), **当值** determined by **排班**, multi-seat **双岗**, node modes (单签/或签/多签), and rule-based escalation to **集团** for major matters. The project's primary goal is resume value (简历加分), targeting enterprise / 央国企 Java roles where Flowable/Activiti is a high-frequency JD keyword.

**Decision.** Use **Flowable** as the workflow engine: approval flows as BPMN process definitions; 会签/或签/单签 via multi-instance tasks + completion conditions; escalation-to-**集团** via exclusive gateways with conditions; audit history via Flowable's history service. The domain-specific resolution — resolving the on-duty approver(s) per **岗位** via **排班**, with **B角** backup and **双岗** seats — is implemented as **custom glue**: task listeners / candidate user/group expressions backed by Java beans over our 岗位/排班/当值 model.

**Why.** The **岗位/排班/当值** resolution needs custom domain logic regardless of engine, so the real choice is whether to also carry a BPM engine. For the resume goal, Flowable provides the recognized keyword and a standard enterprise skill, while the custom resolver glue is where genuine depth is demonstrated (not mere API calls). Flowable's multi-instance and gateway primitives fit 会签 and escalation cleanly.

## Considered options

- **Custom lightweight engine** (the earlier choice) — exact domain fit and strong architecture depth, but lacks the keyword recognition the resume goal rewards. Reversed.
- **Pure Flowable, minimal custom logic** — keyword coverage, but 岗位/当值 is too domain-specific for native features; shallow integration under-demonstrates skill.

## Consequences

- Carry Flowable's runtime weight, learning curve (BPMN, engine API, deployment), and its own DB schema.
- Flowable's tables are global (not legal-entity-scoped); tie process instances to our **法人实体** via business keys / process variables, and enforce entity scoping at the application layer when querying tasks/history.
- The custom resolver (岗位/排班/当值/B角) must be correct and tested — it is the core difficulty and the main skill demonstration.
- Approval-flow definitions (BPMN) must be **versioned** so in-flight applications keep running on their original version.
- **Versioning model (refined):** an **审批流** is *business configuration as data* — rows for nodes / **签批方式** / 岗位-refs, edited by admins via UI (not BPMN). Flowable holds a single generic multi-node approval BPMN as executor; on submission the **current config version is snapshotted** onto the process instance. In-flight applications run against their snapshot; new applications read the latest config. Version isolation therefore lives in the config-snapshot layer — the BPMN process definition is static and never regenerated per config change.
