package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "定时任务查询请求")
public class TaskQueryRequest {

    @Schema(description = "任务名称")
    private String name;

    @Schema(description = "是否启用")
    private Boolean hasStart;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;
}
