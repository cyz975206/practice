package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建用户请求")
public class UserCreateRequest {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "zhangsan", requiredMode = Schema.RequiredMode.REQUIRED)
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "关联人员ID")
    private Long personId;

    @NotBlank(message = "所属机构编码不能为空")
    @Schema(description = "所属机构编码", example = "ORG001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orgCode;

    @Schema(description = "账号状态", example = "1")
    private String status;
}
