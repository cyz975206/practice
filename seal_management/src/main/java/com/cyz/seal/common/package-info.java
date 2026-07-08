/**
 * 公共上下文（Common context）—— 多租户上下文持有者、统一异常处理、基础实体、Result 封装等横切基础设施。
 *
 * <p>LegalEntityContext：请求级持有当前法人实体（从 JWT 解析），供 MyBatis-Plus 多租户插件读取（ADR-0002）。
 * 基础实体携带 legal_entity_id（行级隔离）。
 */
package com.cyz.seal.common;
