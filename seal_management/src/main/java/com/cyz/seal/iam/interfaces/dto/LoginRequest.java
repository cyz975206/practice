package com.cyz.seal.iam.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/** 登录请求。 */
public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
