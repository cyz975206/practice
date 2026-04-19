package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建字典类型请求")
public class DictTypeCreateRequest {

    @NotBlank(message = "字典类型编码不能为空")
    @Size(max = 64, message = "字典类型编码长度不能超过64")
    @Schema(description = "字典类型编码", example = "person_status", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictType;

    @NotBlank(message = "字典类型名称不能为空")
    @Size(max = 64, message = "字典类型名称长度不能超过64")
    @Schema(description = "字典类型名称", example = "人员状态", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictName;

    @Schema(description = "状态", example = "ENABLED")
    private String status;

    @Size(max = 255, message = "备注长度不能超过255")
    @Schema(description = "备注")
    private String remark;
}
