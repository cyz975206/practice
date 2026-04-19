package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "创建字典项请求")
public class DictItemCreateRequest {

    @NotBlank(message = "字典类型编码不能为空")
    @Schema(description = "字典类型编码", example = "person_status", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictType;

    @NotBlank(message = "字典项编码不能为空")
    @Size(max = 64, message = "字典项编码长度不能超过64")
    @Schema(description = "字典项编码", example = "ACTIVE", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictCode;

    @NotBlank(message = "字典项显示名不能为空")
    @Size(max = 64, message = "字典项显示名长度不能超过64")
    @Schema(description = "字典项显示名", example = "在职", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictLabel;

    @Size(max = 255, message = "字典项值长度不能超过255")
    @Schema(description = "字典项值")
    private String dictValue;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "状态", example = "ENABLED")
    private String status;

    @Size(max = 255, message = "备注长度不能超过255")
    @Schema(description = "备注")
    private String remark;
}
