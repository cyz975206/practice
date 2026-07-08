-- 移除 legal_entity.group_id
-- 原因：集团即部署本身（CONTEXT.md：一个部署 = 一个集团，集团是顶层 scope），不单建 Group 表/外键，
-- 故法人实体无需 group_id（同一部署内所有法人实体天然属于该部署的集团）。
-- 用 V2 而非改 V1：V1 已在库中应用、Flyway 校验和固定，改 V1 会致 checksum 不匹配启动失败。
ALTER TABLE legal_entity DROP COLUMN IF EXISTS group_id;
