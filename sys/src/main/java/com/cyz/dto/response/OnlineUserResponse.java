package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "在线用户信息")
public class OnlineUserResponse {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户昵称")
    private String nickname;

    @Schema(description = "所属机构编码")
    private String orgCode;

    @Schema(description = "登录时间戳")
    private long loginTime;

    @Schema(description = "客户端IP")
    private String clientIp;
}
