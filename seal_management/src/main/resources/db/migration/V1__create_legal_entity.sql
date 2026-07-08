-- 法人实体（多租户行级隔离的租户单位，ADR-0002）
-- 该表本身是全局表（不参与 legal_entity_id 过滤），由 MybatisPlusConfig.ignoreTable 排除。
-- id 由 MyBatis-Plus 雪花分配（ASSIGN_ID，分布式友好 ADR-0009），故 BIGINT 非 SERIAL。
CREATE TABLE legal_entity (
    id           BIGINT        PRIMARY KEY,
    group_id     BIGINT        NOT NULL,
    code         VARCHAR(64)   NOT NULL,
    full_name    VARCHAR(255)  NOT NULL,
    short_name   VARCHAR(128),
    entity_type  VARCHAR(32)   NOT NULL,
    status       SMALLINT      NOT NULL DEFAULT 1,
    create_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    create_by    BIGINT,
    update_by    BIGINT,
    deleted      SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT uk_legal_entity_code UNIQUE (code)
);
COMMENT ON TABLE  legal_entity IS '法人实体（集团本部/子公司/分公司），多租户行级隔离的租户单位';
COMMENT ON COLUMN legal_entity.entity_type IS 'GROUP_HQ=集团本部 SUBSIDIARY=子公司 BRANCH=分公司';
COMMENT ON COLUMN legal_entity.status IS '1=启用 0=停用';
COMMENT ON COLUMN legal_entity.deleted IS '逻辑删除：0=未删除 1=已删除';
