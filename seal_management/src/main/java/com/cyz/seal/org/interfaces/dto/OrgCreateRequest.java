package com.cyz.seal.org.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 创建机构请求。parentId 缺省 0（顶级）。
 */
public record OrgCreateRequest(
        @NotBlank String code,
        @NotBlank String name,
        Long parentId,
        Integer sort
) {
}
