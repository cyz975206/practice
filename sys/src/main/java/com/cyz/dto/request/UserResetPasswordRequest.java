package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "重置密码请求")
public class UserResetPasswordRequest {

    @Schema(description = "新密码（可选，不传则自动生成）", example = "newPassword123")
    private String newPassword;
}
