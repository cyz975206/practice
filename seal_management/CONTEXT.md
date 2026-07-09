# Seal Management System (印章管理系统)

A system for managing an organization's official seals — both **physical seals** (custody, borrowing, approval, usage, return, inventory) and **electronic seals** (certificates, signing, verification). The two kinds share the abstract notion of a "seal" but have distinct lifecycles and are managed separately.

## Language

**印章 (Seal)**:
An official stamp belonging to an organization, used to legally authorize documents or transactions. The shared abstraction; every seal is exactly one of the two concrete kinds below.
_Avoid_: 章, 印, stamp, chop (casual speech only — not as canonical terms)

**实体印章 (Physical Seal)**:
A tangible seal engraved from material — e.g. 公章, 财务章, 合同章, 法人章, 发票章 — held in physical custody and lent out for use, then returned.
_Avoid_: 物理印章, 纸质印章, real seal

**电子印章 (Electronic Seal)**:
A digital seal bound to a cryptographic certificate, applied to electronic documents via the **签章接口** to produce legally-recognized e-signatures. It reuses the **印章状态** lifecycle, but its transitions are *certificate-driven* (在用 = valid unexpired cert; expiry → 停用; revocation → 销毁). It has no **保管员** and cannot be **借用** (nothing physical to hold); its **用印方式** is fixed to **电子签章**. It still requires an **印模** (for the **签章接口** to render/stamp).
_Avoid_: 数字印章, e章, digital stamp

**印章类型 (Seal Type)**:
The functional classification of a seal (公章 / 财务章 / 合同章 / 法人章 / 发票章 / etc.), independent of whether it is physical or electronic.
_Avoid_: 印章种类

**集团 (Group)**:
The enterprise group (e.g., a 央企/国企集团) that the system is deployed for. One deployment serves exactly one group. The group is the top-level scope: it contains multiple legal entities and holds cross-entity oversight.
_Avoid_: 集团公司, 总公司, 平台 (平台 is the software; 集团 is the organization)

**法人实体 (Legal Entity)**:
A legally-registered company within the group — the group HQ (集团本部), a subsidiary (子公司), or a branch (分公司). Each legal entity owns its own seals, users, and approval configuration, and its data is isolated from other legal entities. This is the unit of multi-entity isolation in the system.
_Avoid_: 租户 (an implementation alias for the isolation pattern — not a domain term; avoid in domain speech), 法人 (means "legal person" status, not the entity), 公司 (ambiguous), 客户

### 组织与人员主数据 (Org & people master data)

**部门 (Department)**:
An organizational unit within a **法人实体**, arranged as a tree (each 部门 has one parent 部门, up to the entity root) and holding **人员**. When **统一平台** sync is enabled, 部门 is master data synced from it (read-only locally); otherwise it is maintained in the seal system.
_Avoid_: 组织, 机构 (机构 is the collective for 法人实体/部门/岗位, not a single department)

**人员 / 用户 (Person / User)**:
In this system **人员** and **用户** are **one entity** — a person who logs in. The single record carries both the person's identity (name, 工号, **部门**, **职位**) and the login credentials, and belongs to one **法人实体**. **保管员**, **审批人**, and **申请人** are all this entity; it is both the domain subject and the authenticated principal, so domain relationships and audit all key off its single id. When **统一平台** sync is enabled, the identity attributes are master data synced from it.
_Avoid_: 员工 / 账号 (acceptable synonyms); treating 人员 and 用户 as two separate things — they are unified here

### Physical seal usage

**保管员 (Custodian)**:
The person accountable for holding a specific **实体印章** and controlling its use — either stamping on the applicant's behalf (代盖章) or handing the seal over and taking it back (借用). Custody is a **per-seal assignment**, not a system role and not a **岗位**: any active person within the **法人实体** may be designated. A seal has one custodian at a time, but one person may be custodian of multiple seals. The **法人实体管理员** assigns custody and can change it (保管员变更).
_Avoid_: 印章管理员 (ambiguous — sounds like a system administrator), 保管人

**用印申请 (Seal Usage Application)**:
A formal request by a user to use a specific **印章** for a stated purpose, which triggers the approval flow. Submitting an application is not the same as using the seal.
_Avoid_: 申请单, 用印单, 用印请求

**用印申请状态 (Application Status)**:
The lifecycle state of a **用印申请**: 草稿, 审批中, 已拒绝, 已撤销 (withdrawn by applicant), 已审批 (approved, pending fulfillment), 履行中 (being fulfilled — with async sub-states), 已完成, 失败/超时. During 履行中, completion is **channel-driven**: device/integration **用印方式** (一体机 / 印章柜 / 电子签章) advance via device callback/event; **人工代盖章** and non-cabinet **借用** advance via **保管员** manual confirmation. **借用** modes hold a long-lived **借出中** sub-state (a seal may be out for hours or days), governed by a non-return timeout.
_Avoid_: 申请状态 (too generic)

**代盖章 (Stamp-on-Behalf)**:
A usage mode where the **保管员** retains the seal and stamps the document for the applicant; the seal never leaves the custodian's control, so no handover or return is recorded.
_Avoid_: 代盖, 代理盖章

**借用 (Borrow)**:
A usage mode where the applicant takes physical possession of the seal (on-site or off-site), uses it, then returns it to the **保管员**. Involves an explicit handover and a return.
_Avoid_: 外带 (specifically off-site borrow — a subset, not a synonym), 领用

**使用记录 (Usage Record)**:
The logged record of an actual stamping event — seal, applicant, custodian, time, document, number of impressions — produced after an approved **用印申请** is fulfilled.
_Avoid_: 用印记录, 盖章记录

### Roles

系统固定 **4 个角色**，按作用域分两组（GROUP 跨实体 / ENTITY 本实体）：

**超级管理员 (Super Admin, `super_admin`)** — GROUP 作用域:
部署级最高管理员：**唯一可管理 法人实体**，并可跨实体管理用户/配置。**绕过多租户**（跨实体数据可见）。
> 设计已定；多租户绕过（ignore-tenant 旁路）**待实现**——当前代码暂按 ENTITY 作用域处理（见 ADR-0002）。
_Avoid_: **集团管理员**（已并入超级管理员——系统也可能服务小企业而非仅集团，故用通称"超级"），超管

**超级审计员 (Super Auditor, `super_auditor`)** — GROUP 作用域:
部署级只读审计：跨所有 **法人实体** 的查看、统计、审计，不能审批或改数据。**绕过多租户**。即原 **集团审计员**。
> 设计已定；绕过**待实现**（推迟）。
_Avoid_: **集团审计员**（改用 超级审计员 通称），集团监察员

**系统管理员 (Entity Admin, `admin`)** — ENTITY 作用域:
本 **法人实体** 的管理员：管本实体用户/角色/机构（及印章/保管员/审批配置）；看不到他实体数据。即 **法人实体管理员**。
_Avoid_: 子公司管理员 / 分公司管理员（不论实体类型一律用此通称）

**普通用户 (Normal User, `user`)** — ENTITY 作用域:
本 **法人实体** 普通用户，**默认角色**（API 新建用户自动授予）。即 **申请人**——任何提交 **用印申请** 的人。
_Avoid_: 一般用户 / 成员（统一用 普通用户）

**自定义角色（非系统角色）**:
管理员按需自建。如 **审批人**（审批流中的候选审批者，由 **岗位 / 角色 / 规则** 解析）、**集团审批人**（跨实体上报审批，走审批流配置而非固定角色）。在"角色→资源权限表"落地前，自定义角色对系统鉴权是惰性的（不自动获得管理权限）。

### Approval

**审批流 (Approval Flow)**:
An ordered sequence of **审批节点** that a **用印申请** passes through. Configured per combination of **法人实体**, **印章类型**, and usage purpose. It is **business configuration (data)** — edited by admins via UI, not BPMN; a submitted **用印申请** snapshots the config version it started on, so in-flight applications are unaffected by later edits (see ADR-0003).
_Avoid_: 审批流程 (verbose), 工作流 (too generic)

**审批节点 (Approval Node)**:
One step in an **审批流**. A node resolves its candidate approvers via a resolver (by **岗位**, role, specific user, or rule), then applies a **签批方式** to decide when the node is satisfied.
_Avoid_: 审批环节, 节点 (too generic)

**签批方式 (Sign Mode)**:
The sole determinant of *how many candidates must sign* to satisfy a **审批节点** — configurable per node: 单签 (one designated person), 或签 (any one of the candidates), or 多签 (a quorum of N-of-M, including all-M 会签). It is orthogonal to how many candidates a resolver yields: dual-control (四眼原则) is a **双岗** **岗位** (two 当值 candidates) combined with 多签/会签 — never implied by seat count alone.
_Avoid_: 审批方式 (ambiguous)

**待办 (Todo)**:
An actionable item awaiting a user — most commonly an **审批人**'s pending **审批节点** tasks. An approver's work queue is their 待办列表.
_Avoid_: 任务 (too generic), 待办事项

### Positions & duty

**岗位 (Position)**:
An organizational position with approval authority (e.g., a department-head seat). A 岗位 has one or more **seats** (单岗 = one seat, 双岗 = two seats); each seat has a primary holder (正岗 / A角) and a **B角**, and its active holder at any time (the **当值**) is determined by the **排班**. A 岗位 thus yields one **当值** candidate per seat (双岗 → two). Seat count decides *how many candidates* a 岗位 produces — not how many must sign; that is the **签批方式**'s job (the two are orthogonal).
_Avoid_: 职务 (an HR duty title), 职位 (a different concept — see below)

**职位 (HR Position)**:
A person's HR job title/position (e.g., 部门经理) sourced from the **统一平台**. Distinct from **岗位** — a 职位 is an HR attribute of a person; a 岗位 is a seal-approval seat with primary/**B角** holders and **排班**.
_Avoid_: 职务 (acceptable synonym), 头衔

**B角 (Backup / B-role)**:
The backup holder for a **岗位** seat, who acts when the primary holder is off-duty.
_Avoid_: 副岗 (a deputy is a different concept), 替补

**当值 (On Duty)**:
The state of being the currently-active responsible holder for a **岗位** seat at a given time, per the **排班**. Approval routes to on-duty holders.
_Avoid_: 值班 (implies general duty, not position-specific)

**排班 (Duty Roster)**:
The schedule determining which holder (primary or **B角**) of each **岗位** seat is **当值** over time.
_Avoid_: 值班表

### Seal lifecycle

**印章状态 (Seal Status)**:
The lifecycle state of a **印章** — shared by both **实体印章** and **电子印章**: 待启用, 在用, 停用, or 销毁; generally 待启用 → 在用 ⇄ 停用 → 销毁. The *triggers* differ by kind: a **实体印章** is 在用 when it has a **保管员**, and its transitions are custody / handover / **遗失**-driven; an **电子印章** is 在用 when its cryptographic certificate is valid and unexpired, cert expiry auto-suspends to 停用, and revocation retires to 销毁. **公告作废中** (under public invalidation after **遗失**) is a sub-state of **停用** that applies to **实体印章** only; it resolves either back to **在用** (recovered within the notice period) or to **销毁** (notice expired).
_Avoid_: 状态 (too generic)

**借出中 (Lent Out)**:
A transient sub-state of **在用**: the seal is currently in an applicant's possession under **借用** mode, between handover and return.
_Avoid_: 外借中

**遗失 (Lost)**:
An incident on a **实体印章** — reported lost, then it enters **公告作废** (public invalidation to prevent misuse). 公告作废 is **two-phase**: a configurable notice period during which the seal is suspended (**印模** blacklisted, in-flight **用印申请** blocked); if recovered within the period it returns to **在用**, otherwise (XXL-JOB checks notice expiry) it finalizes to **销毁** (irreversible).
_Avoid_: 丢失 (casual), 挂失 (the *report* action, not the incident)

### Numbering

**印章编号 (Seal Number)**:
A unique identifier assigned to a **印章** at intake, generated by its **法人实体**'s **编号规则**. May be physically engraved on a **实体印章**.
_Avoid_: 印章编码 (acceptable synonym)

**编号规则 (Numbering Rule)**:
A configurable rule, defined per **法人实体**, that generates a **印章编号** from segments (e.g., entity code, **印章类型** code, year, **流水号**). Business-defined; each entity may use a different rule.
_Avoid_: 编码规则

**流水号 (Serial Number)**:
The auto-incrementing sequence segment of a **印章编号**. User-modifiable; subject to **跳号** and **占号** logic.
_Avoid_: 序号 (too generic)

**跳号 (Number Skipping)**:
Excluding certain **流水号** values from allocation — avoided, reserved, or already used externally.
_Avoid_: 跳过号码

**占号 (Number Reservation)**:
Reserving a specific **流水号** value ahead of time — the serial is manually set to a chosen value, which then becomes unavailable to auto-allocation.
_Avoid_: 预留号

### Devices & integration

**印模 (Seal Impression)**:
The digital image of a **印章** — every seal (physical or electronic) has one. Required by the **打印用印一体机** (physical) and the **签章接口** (electronic) to render/stamp the seal.
_Avoid_: 印章图片 (too generic), 印章图像

**制作印模 (Impression Creation)**:
Producing a seal's **印模**, typically by scanning a **实体印章**'s impression or generating from a design.
_Avoid_: 印章扫描 (only one method)

**拖章定位 (Drag-to-Position)**:
Positioning a seal on a document by dragging, to produce the stamp **坐标**. Required by the **打印用印一体机** and the **签章接口**; not used by the **智能印章柜**.
_Avoid_: 拖拽盖章, 定位盖章

**坐标 (Coordinates)**:
The position on a document page where a seal is stamped/signed, produced by **拖章定位**.
_Avoid_: 位置 (too generic)

**打印用印一体机 (Print-Seal Machine)**:
A device that prints a document and auto-stamps the **实体印章** at given **坐标**. The seal is loaded in the machine and stamps it; the seal is never handed to a person.
_Avoid_: 盖章机; 一体机 (acceptable shorthand)

**智能印章柜 (Smart Seal Cabinet)**:
A locker-style device (蜂巢柜-like) that stores **实体印章** and controls access via **授权码**. It does NOT auto-stamp — it only controls storage and tracks door open/return. The cabinet is a **storage and access intermediary** for a **借用**: it does NOT replace the seal's human **保管员**, who remains accountable (non-return, **遗失**, etc.). The **授权码** lets the applicant self-serve the handover/return that the **保管员** would otherwise do by hand.
_Avoid_: 印章柜 (acceptable shorthand), 印章盒

**授权码 (Authorization Code)**:
A code generated by the system on an approved **用印申请**, used to open a specific **智能印章柜** door for a specific seal.
_Avoid_: 取章码, 开柜码

**签章接口 (E-Sign API)**:
An external enterprise/government e-sign service that performs real cryptographic signing of a document at given **坐标** using a **印模**, for **电子印章**.
_Avoid_: 电子签章平台 (that's the vendor, not the interface)

**用印方式 (Fulfillment Channel)**:
The concrete mechanism fulfilling an approved **用印申请**: 人工代盖章 (custodian stamps manually), 一体机自动盖章 (machine auto-stamps via **印模** + **坐标**), 印章柜借用 (cabinet borrow via **授权码**), or 电子签章 (e-sign API via **印模** + **坐标**).
_Avoid_: 用印类型 (ambiguous)

### Enterprise integrations (三方接入)

> The system is a product deployed for different enterprises, each with its own surrounding systems. These are the categories of external systems the seal system integrates with per deployment.

**统一平台 (Unified Platform)**:
An enterprise's master system for organization and personnel. The seal system syncs 机构 (法人实体/部门/岗位) and 人员 from it as the source of truth for org/people master data.
_Avoid_: 主数据平台 (acceptable synonym), 中台 (too generic)

**影像平台 (Image/Document Platform)**:
An enterprise's centralized document/image archive. The seal system pushes 用印 documents, scanned stamped evidence, and signed PDFs to it.
_Avoid_: 文档平台, 影像系统

**调度系统 (Dispatch System)**:
An enterprise's task-dispatch / cross-system workflow-triggering system.
_Avoid_: 工单系统 (different concept)

**日志系统 (Log System)**:
An enterprise's centralized logging platform; the seal system ships audit/operation logs to it.
_Avoid_: 日志平台 (acceptable synonym)

**大数据平台 (Big Data Platform)**:
An enterprise's analytics platform; the seal system syncs usage/audit data to it for enterprise-level analytics.
_Avoid_: 数仓 (related but distinct)

## Relationships

- A **印章** is exactly one of: a **实体印章** or an **电子印章** — never both; both share **印章状态**, but physical transitions are custody-driven and electronic transitions are certificate-driven (an **电子印章** has no **保管员**/**借用** and uses only **电子签章**)
- Both kinds carry a **印章类型**
- A **集团** contains many **法人实体**
- Every **印章**, user, and configuration belongs to exactly one **法人实体**
- A **部门** is a tree within a **法人实体** and holds **人员**; **岗位** (when present) sit under a **部门**
- A **人员** belongs to one **法人实体** and one **部门**; **保管员**, **审批人**, and **申请人** are all **人员**
- **人员** and **用户** are one entity; both domain relationships and audit key off its single id
- A **实体印章** has one **保管员** at a time; custody is a per-seal assignment (not a role or **岗位**), and one person may hold custody of multiple seals
- An approved **用印申请**, once fulfilled, produces a **使用记录**
- In **借用** mode, fulfillment includes a handover and a return; in **代盖章** mode it does not
- **Role scope** is a property of the role: **超级管理员 / 超级审计员** are **GROUP-scoped** (cross-**法人实体**, bypass row isolation — *designed, bypass pending implementation*, per ADR-0002); **系统管理员 / 普通用户** are **ENTITY-scoped** (confined to the holder's **法人实体**). The two GROUP roles manage/audit across the whole deployment; the ENTITY roles see only their own entity
- A **用印申请** is routed through an **审批流** of **审批节点**; each node resolves candidates (by **岗位**, role, user, or rule) and is satisfied per its **签批方式**. Candidate *count* (a **双岗** **岗位** yields two **当值**) and *sign count* (**签批方式**) are orthogonal — dual-control is 双岗 + 多签/会签
- A **岗位** seat's active holder is its **当值** member, set by the **排班**; a **B角** acts when the primary is off-duty
- A **实体印章** has a **印章状态**; **借出中** is a transient sub-state of **在用**
- A **借用** not returned past its max grace period escalates to a **遗失** incident (→ 公告作废), not merely a 失败/超时 application — an unreturned seal is a loss risk
- A **遗失** incident forces public invalidation and a move toward **销毁**
- A **印章** has a **印模** (created via 制作印模); **拖章定位** produces the **坐标** for stamping/signing
- An approved **用印申请** is fulfilled via one **用印方式**: 一体机自动盖章 and 电子签章 require **印模** + **坐标**; 印章柜借用 requires an **授权码**; 人工代盖章 needs neither device
- 人工代盖章 and 一体机自动盖章 are forms of **代盖章** (seal not handed to the applicant); 印章柜借用 is a form of **借用**
- A **印章编号** is generated per **法人实体** by that entity's **编号规则**; its **流水号** segment is auto-allocated but user-modifiable
- **流水号** allocation is concurrency-safe and picks the **lowest available** number; **占号** consumes a specific number (in-between numbers remain available for future allocation), while **跳号** permanently excludes a number — a registry of used/skipped numbers prevents collisions
- When **统一平台** sync is enabled, synced entities (**法人实体** / 部门 / 人员 / **职位**) are read-only locally; **岗位** / **排班** / 角色 / **保管员** are maintained in the seal system and reference the synced people/org

## Example dialogue

> **Dev:** "When a user applies to use a **印章**, does the workflow differ by kind?"
> **Domain expert:** "Yes. A **实体印章** application is about *borrowing the physical object* — approval, handover, return. An **电子印章** application is about *being authorized to sign a document electronically* — there's nothing to hand over or return."

## Flagged ambiguities

- "印章" is used loosely to mean both physical and electronic kinds — resolved: 印章 is the shared abstraction; the two kinds are distinct concepts with separate lifecycles.
- "多租户 / 租户" was initially read as public-SaaS multi-tenancy — resolved: in this system it means **多法人** (multiple legal entities within one enterprise **集团**). Canonical term is **法人实体**; 租户 survives only as an implementation-level alias for the isolation pattern.
- "人员 / 用户" were used interchangeably — resolved: in this system they are **one entity** (a person who logs in, carrying both identity and credentials), not separate.
- 角色模型 — resolved: 系统固定 **4 角色**——超级管理员 / 超级审计员（GROUP，绕过多租户，**绕过待实现**）+ 系统管理员 / 普通用户（ENTITY）。旧称映射：集团管理员→超级管理员、集团审计员→超级审计员、法人实体管理员→系统管理员、申请人→普通用户；审批人 / 集团审批人 改为自定义角色（非系统角色）。
