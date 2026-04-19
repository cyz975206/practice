package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "用户信息")
public class UserResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "关联人员ID")
    private Long personId;

    @Schema(description = "所属机构编码")
    private String orgCode;

    @Schema(description = "所属机构名称")
    private String orgName;

    @Schema(description = "账号状态")
    private String status;

    @Schema(description = "登录失败次数")
    private Integer loginFailCount;

    @Schema(description = "最后登录时间")
    private LocalDateTime lastLoginTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    @Schema(description = "角色名称列表")
    private List<String> roleNames;

    @Schema(description = "是否在线")
    private Boolean online;
}
