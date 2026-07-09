-- 扩展 sys_user：合并人员属性（CONTEXT.md：人员=用户，一个实体）。
-- 加：real_name / employee_no / phone / org_id / position（人员信息）+ external_id / sync_source（统一平台预留，ADR-0007）。
-- 删：nickname（人员真名改由 real_name 承担）。
-- 用 V5 而非改 V3：V3 已应用、Flyway 校验和固定，改 V3 会 checksum 不匹配启动失败。
ALTER TABLE sys_user ADD COLUMN real_name   VARCHAR(128);
ALTER TABLE sys_user ADD COLUMN employee_no  VARCHAR(64);
ALTER TABLE sys_user ADD COLUMN phone        VARCHAR(32);
ALTER TABLE sys_user ADD COLUMN org_id       BIGINT;
ALTER TABLE sys_user ADD COLUMN position     VARCHAR(128);
ALTER TABLE sys_user ADD COLUMN external_id  VARCHAR(64);
ALTER TABLE sys_user ADD COLUMN sync_source  VARCHAR(16);
ALTER TABLE sys_user DROP COLUMN IF EXISTS nickname;
-- 工号在本法人实体内唯一（标准 UNIQUE 约束；PG 下 NULL 互不冲突，故可空，信创可迁 ADR-0001）
ALTER TABLE sys_user ADD CONSTRAINT uk_sys_user_entity_empno UNIQUE (legal_entity_id, employee_no);
CREATE INDEX idx_sys_user_org ON sys_user (legal_entity_id, org_id);
COMMENT ON COLUMN sys_user.real_name   IS '真实姓名（人员信息）';
COMMENT ON COLUMN sys_user.employee_no IS '工号（法人实体内唯一，可空）';
COMMENT ON COLUMN sys_user.org_id      IS '所属部门 sys_org.id（同实体，应用层校验）';
COMMENT ON COLUMN sys_user.position    IS '职位（HR 头衔；统一平台同步后为准）';
COMMENT ON COLUMN sys_user.external_id IS '统一平台稳定 ID（同步预留，ADR-0007）';
