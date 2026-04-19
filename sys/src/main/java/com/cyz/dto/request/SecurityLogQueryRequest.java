package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "安全日志查询请求")
public class SecurityLogQueryRequest {

    @Schema(description = "风险类型（auth_fail/session_expired/ss_conflict等）")
    private String riskType;

    @Schema(description = "风险等级（low/medium/high）")
    private String riskLevel;

    @Schema(description = "处理状态（unhandled/handled/ignored）")
    private String handleStatus;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer size = 10;
}
