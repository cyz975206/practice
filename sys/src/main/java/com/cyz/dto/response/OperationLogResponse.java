package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "操作日志信息")
public class OperationLogResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "操作人ID")
    private Long operatorId;

    @Schema(description = "操作人姓名")
    private String operatorName;

    @Schema(description = "操作IP")
    private String operatorIp;

    @Schema(description = "操作人所属机构号")
    private String operatorOrgCode;

    @Schema(description = "操作人所属机构")
    private String operatorOrg;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作类型")
    private String operationType;

    @Schema(description = "操作内容描述")
    private String operationContent;

    @Schema(description = "请求参数")
    private String requestParams;

    @Schema(description = "操作结果")
    private String operationResult;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "操作时间")
    private LocalDateTime operationTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
