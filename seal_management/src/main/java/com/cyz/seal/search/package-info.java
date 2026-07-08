/**
 * 搜索分析上下文（Search context）—— Elasticsearch 索引同步 + 查询（审计/用印/统计）。
 *
 * <p>经 RabbitMQ 消费者写入 ES（非业务事务内双写，最终一致 + 幂等）。文档带 legal_entity_id；
 * 集团视角走特权查询。ES 仅作检索/统计读模型，PG 为事实来源。
 */
package com.cyz.seal.search;
