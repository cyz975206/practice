package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新系统配置请求")
public class ConfigUpdateRequest {

    @Schema(description = "配置名称", example = "用户初始密码")
    private String configName;

    @Schema(description = "配置值", example = "123456")
    private String configValue;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "排序", example = "0")
    private Integer sort;
}
