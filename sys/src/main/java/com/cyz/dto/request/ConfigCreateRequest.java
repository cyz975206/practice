package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建系统配置请求")
public class ConfigCreateRequest {

    @NotBlank(message = "配置键不能为空")
    @Schema(description = "配置键", example = "sys.user.init-password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String configKey;

    @Schema(description = "配置名称", example = "用户初始密码")
    private String configName;

    @Schema(description = "配置值", example = "123456")
    private String configValue;

    @Schema(description = "备注说明")
    private String remark;

    @Schema(description = "排序", example = "0")
    private Integer sort;
}
