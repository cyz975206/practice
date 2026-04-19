package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "更新字典类型请求")
public class DictTypeUpdateRequest {

    @Size(max = 64, message = "字典类型名称长度不能超过64")
    @Schema(description = "字典类型名称", example = "人员状态")
    private String dictName;

    @Schema(description = "状态", example = "ENABLED")
    private String status;

    @Size(max = 255, message = "备注长度不能超过255")
    @Schema(description = "备注")
    private String remark;
}
