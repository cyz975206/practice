/**
 * 调度上下文（Scheduler context）—— XXL-JOB handler（分布式调度，ADR-0005 / ADR-0009）。
 *
 * <p>任务：借用超时提醒（三档阶梯转遗失）、排班轮值（推进当值切换 A角↔B角）、
 * 集团报表、数据归档/清理、遗失公告作废期满处理。调度集中管理，执行器独立线程池不阻塞 Web 请求。
 */
package com.cyz.seal.scheduler;
