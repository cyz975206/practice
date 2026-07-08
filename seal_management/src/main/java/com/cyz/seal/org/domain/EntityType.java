package com.cyz.seal.org.domain;

/**
 * 法人实体类型（CONTEXT.md：集团本部 / 子公司 / 分公司）。
 * 存储为枚举名（VARCHAR），由 MybatisEnumTypeHandler 处理。
 */
public enum EntityType {
    GROUP_HQ,     // 集团本部
    SUBSIDIARY,   // 子公司
    BRANCH        // 分公司
}
