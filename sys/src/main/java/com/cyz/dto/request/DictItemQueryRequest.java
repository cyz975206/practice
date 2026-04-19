package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "字典项查询请求")
public class DictItemQueryRequest {

    @NotBlank(message = "字典类型编码不能为空")
    @Schema(description = "字典类型编码", example = "person_status", requiredMode = Schema.RequiredMode.REQUIRED)
    private String dictType;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer size = 10;
}
