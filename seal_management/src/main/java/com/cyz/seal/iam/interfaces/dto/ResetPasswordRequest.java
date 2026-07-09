package com.cyz.seal.iam.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

/** 重置密码请求。 */
public record ResetPasswordRequest(@NotBlank String password) {
}
