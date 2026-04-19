package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新角色请求")
public class RoleUpdateRequest {

    @Schema(description = "角色名称", example = "管理员")
    private String roleName;

    @Schema(description = "角色描述", example = "系统管理员角色")
    private String roleDesc;

    @Schema(description = "角色状态（1-启用 0-禁用）", example = "1")
    private String status;

    @Schema(description = "排序", example = "0")
    private Integer sort;
}
