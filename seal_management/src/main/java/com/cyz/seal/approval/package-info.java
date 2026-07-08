/**
 * 审批上下文（Approval context）—— Flowable 集成 + 岗位/排班解析胶水（ADR-0003，核心难点）。
 *
 * <p>审批流 = 业务配置（数据，非 BPMN）+ 每申请快照；Flowable 用通用多节点审批 BPMN 作执行器。
 * 自定义解析胶水按 岗位 + 排班 解析当值审批人，处理 B角顶替与双岗；双岗 ⊥ 签批方式（双控=双岗+多签）。
 */
package com.cyz.seal.approval;
