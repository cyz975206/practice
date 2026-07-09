package com.cyz.seal.org.interfaces.dto;

/**
 * 更新机构请求。各字段可选；parentId 变更触发子树 ancestors 重算。
 */
public record OrgUpdateRequest(
        String name,
        Long parentId,
        Integer sort,
        Integer status
) {
}
