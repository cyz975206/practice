package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "字典类型查询请求")
public class DictTypeQueryRequest {

    @Schema(description = "字典类型编码（模糊）")
    private String dictType;

    @Schema(description = "字典类型名称（模糊）")
    private String dictName;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer size = 10;
}
