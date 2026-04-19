package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "系统日志信息")
public class SystemLogResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "日志级别（info/warn/error）")
    private String logLevel;

    @Schema(description = "日志模块")
    private String logModule;

    @Schema(description = "日志内容")
    private String logContent;

    @Schema(description = "异常堆栈")
    private String exceptionStack;

    @Schema(description = "发生时间")
    private LocalDateTime occurTime;

    @Schema(description = "服务器IP")
    private String serverIp;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
