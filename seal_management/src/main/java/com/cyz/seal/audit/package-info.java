/**
 * 审计日志上下文（Audit context）—— @OperationLog 注解 + AOP 切面，异步入库。
 *
 * <p>链路：切面投递 RabbitMQ → 消费者写 PostgreSQL（事实源）+ Elasticsearch（检索）。
 * 独立审计表带 legal_entity_id；集团审计员可跨实体查询与导出（ADR-0002 GROUP 旁路）。
 */
package com.cyz.seal.audit;
