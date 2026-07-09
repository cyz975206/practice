package com.cyz.seal.iam.interfaces.dto;

/**
 * 更新角色请求。仅 name/status 可改；code 与 scope 锁定（创建时定）。
 */
public record RoleUpdateRequest(
        String name,
        Integer status
) {
}
