package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "机构树节点")
public class OrgTreeResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "机构编码")
    private String orgCode;

    @Schema(description = "机构简称")
    private String orgShortName;

    @Schema(description = "机构全称")
    private String orgFullName;

    @Schema(description = "机构等级")
    private String orgLevel;

    @Schema(description = "上级机构编码")
    private String parentOrgCode;

    @Schema(description = "负责人用户ID")
    private Long leaderUserId;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "子节点")
    private List<OrgTreeResponse> children;
}
