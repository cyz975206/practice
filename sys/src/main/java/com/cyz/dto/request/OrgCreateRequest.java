package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "创建机构请求")
public class OrgCreateRequest {

    @NotBlank(message = "机构简称不能为空")
    @Schema(description = "机构简称", example = "技术部", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orgShortName;

    @NotBlank(message = "机构全称不能为空")
    @Schema(description = "机构全称", example = "技术研发部", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orgFullName;

    @NotNull(message = "机构等级不能为空")
    @Schema(description = "机构等级", example = "DEPARTMENT", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orgLevel;

    @Schema(description = "上级机构编码")
    private String parentOrgCode;

    @Schema(description = "负责人用户ID")
    private Long leaderUserId;

    @Schema(description = "状态", example = "ENABLED")
    private String status;

    @Schema(description = "排序号")
    private Integer sort;
}
