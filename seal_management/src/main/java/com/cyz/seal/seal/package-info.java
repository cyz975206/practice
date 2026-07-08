/**
 * 印章上下文（Seal context）—— 实体印章 / 电子印章的领域模型、用例与持久化。
 *
 * <p>分层：domain（Seal / SealStatus / Impression 印模 / NumberingRule 编号规则）/
 * application（建章入库、保管员变更、盘点、销毁）/
 * infrastructure（MyBatis-Plus mapper、流水号分配器 占号/跳号/并发安全 ADR-0008）/
 * interfaces（REST）。
 */
package com.cyz.seal.seal;
