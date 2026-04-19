package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "用户查询请求")
public class UserQueryRequest {

    @Schema(description = "用户名", example = "zhangsan")
    private String username;

    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    @Schema(description = "所属机构编码", example = "ORG001")
    private String orgCode;

    @Schema(description = "账号状态", example = "1")
    private String status;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;
}
