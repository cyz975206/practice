-- IAM：角色 / 用户 / 用户角色（均按法人实体隔离，租户表，ADR-0002）
-- legal_entity_id 列必须存在（多租户拦截器在 SQL 层注入该列）；实体侧不映射该字段。
CREATE TABLE sys_role (
    id              BIGINT       PRIMARY KEY,
    legal_entity_id BIGINT       NOT NULL,
    code            VARCHAR(64)  NOT NULL,
    name            VARCHAR(128) NOT NULL,
    scope           VARCHAR(16)  NOT NULL DEFAULT 'ENTITY',
    status          SMALLINT     NOT NULL DEFAULT 1,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT,
    update_by       BIGINT,
    deleted         SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_role_entity_code UNIQUE (legal_entity_id, code)
);
COMMENT ON TABLE  sys_role IS '角色（按法人实体隔离）';
COMMENT ON COLUMN sys_role.scope IS '作用域：ENTITY=仅本法人实体；GROUP=跨实体(集团级,后续)';
COMMENT ON COLUMN sys_role.status IS '1=启用 0=停用';

CREATE TABLE sys_user (
    id              BIGINT       PRIMARY KEY,
    legal_entity_id BIGINT       NOT NULL,
    username        VARCHAR(64)  NOT NULL,
    password        VARCHAR(128) NOT NULL,
    nickname        VARCHAR(128),
    status          SMALLINT     NOT NULL DEFAULT 1,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT,
    update_by       BIGINT,
    deleted         SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_user_username UNIQUE (username)
);
COMMENT ON TABLE sys_user IS '用户（按法人实体隔离；username 全局唯一，便于登录消歧）';
COMMENT ON COLUMN sys_user.password IS 'BCrypt 哈希';

CREATE TABLE sys_user_role (
    id              BIGINT       PRIMARY KEY,
    legal_entity_id BIGINT       NOT NULL,
    user_id         BIGINT       NOT NULL,
    role_id         BIGINT       NOT NULL,
    create_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by       BIGINT,
    update_by       BIGINT,
    deleted         SMALLINT     NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_user_role UNIQUE (legal_entity_id, user_id, role_id)
);
COMMENT ON TABLE sys_user_role IS '用户-角色关联（按法人实体隔离）';

CREATE INDEX idx_sys_user_role_user ON sys_user_role (legal_entity_id, user_id);
CREATE INDEX idx_sys_user_role_role ON sys_user_role (legal_entity_id, role_id);
