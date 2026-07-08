/**
 * 身份与访问上下文（IAM context）—— 用户 / 角色 / 权限 / Spring Security + JWT。
 *
 * <p>角色作用域 ENTITY（仅本法人实体）/ GROUP（跨法人实体，ADR-0002）；集团级角色触发"忽略租户"旁路。
 * 法人实体上下文从 JWT claim 解析，注入多租户插件（无状态，ADR-0009）。
 */
package com.cyz.seal.iam;
