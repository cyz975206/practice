package com.cyz.seal.iam.domain;

/**
 * 角色作用域（ADR-0002）。
 * <ul>
 *   <li>{@link #ENTITY} —— 仅本法人实体（受 legal_entity_id 隔离）。</li>
 *   <li>{@link #GROUP} —— 跨法人实体（集团级角色，触发"忽略租户"旁路；后续阶段实现）。</li>
 * </ul>
 * 当前里程碑仅用 ENTITY；GROUP 旁路随集团级角色后续实现。
 */
public enum RoleScope {
    ENTITY,
    GROUP
}
