package com.cyz.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@Schema(description = "操作日志查询请求")
public class OperationLogQueryRequest {

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作类型（add/edit/delete/import/export/change_status）")
    private String operationType;

    @Schema(description = "操作结果（success/fail）")
    private String result;

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
