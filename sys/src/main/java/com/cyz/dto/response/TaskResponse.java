package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "定时任务信息")
public class TaskResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "任务名称")
    private String name;

    @Schema(description = "服务名称")
    private String serviceName;

    @Schema(description = "方法路径")
    private String funPath;

    @Schema(description = "Cron表达式")
    private String cron;

    @Schema(description = "是否启用")
    private Boolean hasStart;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
