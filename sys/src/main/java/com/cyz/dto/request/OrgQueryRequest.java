package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "机构查询请求")
public class OrgQueryRequest {

    @Schema(description = "机构简称（模糊）")
    private String orgShortName;

    @Schema(description = "机构等级")
    private String orgLevel;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "上级机构编码")
    private String parentOrgCode;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer size = 10;
}
