/**
 * 通知上下文（Notification context）—— 通知 ports + adapters（站内信/企微/钉钉/邮件）+ MQ 生产/消费。
 *
 * <p>站内信实时推送经 WebSocket(STOMP) → RabbitMQ STOMP relay 跨实例路由（ADR-0009）。
 * 待办（Todo）= 审批人待处理的审批节点任务。
 */
package com.cyz.seal.notification;
