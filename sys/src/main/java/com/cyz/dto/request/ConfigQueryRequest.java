package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统配置查询请求")
public class ConfigQueryRequest {

    @Schema(description = "配置键", example = "sys.user")
    private String configKey;

    @Schema(description = "配置名称", example = "用户")
    private String configName;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;
}
