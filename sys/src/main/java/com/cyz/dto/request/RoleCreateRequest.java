package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建角色请求")
public class RoleCreateRequest {

    @NotBlank(message = "角色编码不能为空")
    @Schema(description = "角色编码", example = "admin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleCode;

    @NotBlank(message = "角色名称不能为空")
    @Schema(description = "角色名称", example = "管理员", requiredMode = Schema.RequiredMode.REQUIRED)
    private String roleName;

    @Schema(description = "角色描述", example = "系统管理员角色")
    private String roleDesc;

    @Schema(description = "角色状态（1-启用 0-禁用）", example = "1")
    private String status;

    @Schema(description = "排序", example = "0")
    private Integer sort;
}
