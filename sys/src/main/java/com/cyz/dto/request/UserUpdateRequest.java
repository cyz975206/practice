package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新用户请求")
public class UserUpdateRequest {

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "关联人员ID")
    private Long personId;

    @Schema(description = "所属机构编码", example = "ORG001")
    private String orgCode;

    @Schema(description = "账号状态", example = "1")
    private String status;
}
