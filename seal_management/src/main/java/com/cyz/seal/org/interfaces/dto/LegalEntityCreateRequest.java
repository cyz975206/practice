package com.cyz.seal.org.interfaces.dto;

import com.cyz.seal.org.domain.EntityType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 创建法人实体请求。
 */
public record LegalEntityCreateRequest(
        @NotNull Long groupId,
        @NotBlank String code,
        @NotBlank String fullName,
        String shortName,
        @NotNull EntityType entityType
) {
}
