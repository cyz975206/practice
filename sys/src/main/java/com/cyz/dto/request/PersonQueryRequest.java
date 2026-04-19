package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "人员查询请求")
public class PersonQueryRequest {

    @Schema(description = "姓名（模糊）")
    private String fullName;

    @Schema(description = "员工工号")
    private String staffNum;

    @Schema(description = "所属机构编码")
    private String orgCode;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer size = 10;
}
