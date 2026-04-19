package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建定时任务请求")
public class TaskCreateRequest {

    @NotBlank(message = "任务名称不能为空")
    @Schema(description = "任务名称", example = "日志归档任务", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "服务名称不能为空")
    @Schema(description = "服务名称（标识所属服务实例）", example = "sys-management", requiredMode = Schema.RequiredMode.REQUIRED)
    private String serviceName;

    @NotBlank(message = "方法路径不能为空")
    @Schema(description = "方法路径（格式: beanName.methodName）", example = "logArchiveTask.archiveLogs", requiredMode = Schema.RequiredMode.REQUIRED)
    private String funPath;

    @NotBlank(message = "Cron表达式不能为空")
    @Schema(description = "Cron表达式", example = "0 0 2 * * ?", requiredMode = Schema.RequiredMode.REQUIRED)
    private String cron;

    @Schema(description = "是否启用", example = "false")
    private Boolean hasStart = false;
}
