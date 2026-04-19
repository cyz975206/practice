package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建菜单请求")
public class MenuCreateRequest {

    @NotBlank(message = "菜单编码不能为空")
    @Schema(description = "菜单编码", example = "system", requiredMode = Schema.RequiredMode.REQUIRED)
    private String menuCode;

    @NotBlank(message = "菜单名称不能为空")
    @Schema(description = "菜单名称", example = "系统管理", requiredMode = Schema.RequiredMode.REQUIRED)
    private String menuName;

    @NotBlank(message = "菜单类型不能为空")
    @Schema(description = "菜单类型（D-目录 M-菜单 B-按钮）", example = "D", requiredMode = Schema.RequiredMode.REQUIRED)
    private String menuType;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "路由地址", example = "/system")
    private String path;

    @Schema(description = "组件路径", example = "system/index")
    private String component;

    @Schema(description = "权限标识", example = "system:user:list")
    private String perms;

    @Schema(description = "图标", example = "system")
    private String icon;

    @Schema(description = "是否外链（0-否 1-是）", example = "0")
    private Boolean isFrame;

    @Schema(description = "排序", example = "0")
    private Integer sort;

    @Schema(description = "菜单状态（1-启用 0-禁用）", example = "1")
    private String status;
}
