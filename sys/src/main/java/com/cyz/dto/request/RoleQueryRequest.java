package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "角色查询请求")
public class RoleQueryRequest {

    @Schema(description = "角色名称", example = "管理员")
    private String roleName;

    @Schema(description = "角色状态（1-启用 0-禁用）", example = "1")
    private String status;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;
}
