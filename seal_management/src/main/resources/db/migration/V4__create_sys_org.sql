-- 部门 / 机构（按法人实体隔离的租户表，ADR-0002）。
-- 树形：parent_id 自引用（0=顶级），ancestors 物化路径（"0," / "0,100,"…）便于查子树/祖先链与移动重算。
-- 统一平台同步预留 external_id / sync_source（ADR-0007），本期不接入。
-- id 由 MyBatis-Plus 雪花分配（ASSIGN_ID，分布式友好 ADR-0009），故 BIGINT 非 SERIAL。
CREATE TABLE sys_org (
    id              BIGINT       PRIMARY KEY,
    legal_entity_id BIGINT       NOT NULL,
    parent_id       BIGINT       NOT NULL DEFAULT 0,
    code            VARCHAR(64)  NOT NULL,
    name            VARCHAR(128) NOT NULL,
    ancestors       VARCHAR(500) NOT NULL,
    sort            INTEGER      NOT NULL DEFAULT 0,
    status          SMALLINT     NOT NULL DEFAULT 1,
    external_id     VARCHAR(64),
    sync_source     VARCHAR(16),
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT,
    update_by       BIGINT,
    deleted         SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_org_entity_code UNIQUE (legal_entity_id, code)
);
COMMENT ON TABLE  sys_org IS '部门/机构（按法人实体隔离；树形 parent_id + ancestors 物化路径）';
COMMENT ON COLUMN sys_org.parent_id IS '父机构 ID，0=顶级';
COMMENT ON COLUMN sys_org.ancestors IS '祖先链物化路径，逗号分隔末尾带逗号，如 0, 或 0,100,';
COMMENT ON COLUMN sys_org.status IS '1=启用 0=停用';
COMMENT ON COLUMN sys_org.external_id IS '统一平台稳定 ID（同步预留，ADR-0007）';
CREATE INDEX idx_sys_org_parent ON sys_org (legal_entity_id, parent_id);
