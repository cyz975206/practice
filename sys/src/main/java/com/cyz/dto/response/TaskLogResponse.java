package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "任务日志信息")
public class TaskLogResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "任务名称")
    private String taskName;

    @Schema(description = "服务名称")
    private String serviceName;

    @Schema(description = "方法路径")
    private String funPath;

    @Schema(description = "Cron表达式")
    private String cron;

    @Schema(description = "执行结果（1=成功 0=失败）")
    private Integer runResult;

    @Schema(description = "执行日志")
    private String runLog;

    @Schema(description = "执行耗时(ms)")
    private Long durationMs;

    @Schema(description = "执行时间")
    private LocalDateTime createTime;
}
