package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录用户信息")
public class LoginUserResponse {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "所属机构编码")
    private String orgCode;

    @Schema(description = "角色编码列表")
    private List<String> roles;

    @Schema(description = "权限标识列表")
    private List<String> perms;
}
