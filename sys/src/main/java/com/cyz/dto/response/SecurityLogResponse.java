package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "安全日志信息")
public class SecurityLogResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人名称")
    private String operatorName;

    @Schema(description = "操作IP")
    private String operatorIp;

    @Schema(description = "风险类型")
    private String riskType;

    @Schema(description = "风险描述")
    private String riskContent;

    @Schema(description = "请求接口地址")
    private String requestUrl;

    @Schema(description = "风险等级（low/medium/high）")
    private String riskLevel;

    @Schema(description = "处理状态（unhandled/handled/ignored）")
    private String handleStatus;

    @Schema(description = "处理人ID")
    private Long handleUserId;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "处理备注")
    private String handleNote;

    @Schema(description = "发生时间")
    private LocalDateTime occurTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
