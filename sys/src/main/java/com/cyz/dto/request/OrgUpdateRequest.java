package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新机构请求")
public class OrgUpdateRequest {

    @Schema(description = "机构简称", example = "技术部")
    private String orgShortName;

    @Schema(description = "机构全称", example = "技术研发部")
    private String orgFullName;

    @Schema(description = "机构等级", example = "DEPARTMENT")
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
