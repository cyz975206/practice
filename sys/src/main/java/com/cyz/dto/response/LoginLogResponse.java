package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "登录日志信息")
public class LoginLogResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "登录IP")
    private String loginIp;

    @Schema(description = "登录设备/浏览器信息")
    private String loginDevice;

    @Schema(description = "登录类型（login/logout/reset_pwd/unlock）")
    private String loginType;

    @Schema(description = "登录结果（success/fail）")
    private String loginResult;

    @Schema(description = "失败原因")
    private String failReason;

    @Schema(description = "登录时间")
    private LocalDateTime loginTime;

    @Schema(description = "登出时间")
    private LocalDateTime logoutTime;

    @Schema(description = "是否异常登录")
    private Boolean isAbnormal;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
