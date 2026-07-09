package com.cyz.seal.iam.interfaces.dto;

import com.cyz.seal.iam.domain.RoleScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record RoleCreateRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull RoleScope scope
) {
}
