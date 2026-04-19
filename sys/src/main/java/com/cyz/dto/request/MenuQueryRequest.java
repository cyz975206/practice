package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "菜单查询请求")
public class MenuQueryRequest {

    @Schema(description = "菜单名称", example = "系统管理")
    private String menuName;

    @Schema(description = "菜单类型（D-目录 M-菜单 B-按钮）", example = "D")
    private String menuType;

    @Schema(description = "菜单状态（1-启用 0-禁用）", example = "1")
    private String status;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;
}
