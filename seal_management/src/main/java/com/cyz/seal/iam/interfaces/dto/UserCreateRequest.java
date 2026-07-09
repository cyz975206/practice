package com.cyz.seal.iam.interfaces.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreateRequest(
        @NotBlank String username,
        @NotBlank String password,
        String nickname
) {
}
