package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新定时任务请求")
public class TaskUpdateRequest {

    @Schema(description = "任务名称", example = "日志归档任务")
    private String name;

    @Schema(description = "方法路径", example = "logArchiveTask.archiveLogs")
    private String funPath;

    @Schema(description = "Cron表达式", example = "0 0 2 * * ?")
    private String cron;

    @Schema(description = "是否启用", example = "true")
    private Boolean hasStart;
}
