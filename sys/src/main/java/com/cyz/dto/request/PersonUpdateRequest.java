package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "更新人员请求")
public class PersonUpdateRequest {

    @Schema(description = "姓", example = "张")
    private String surname;

    @Schema(description = "名", example = "三")
    private String givenName;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "员工工号", example = "EMP001")
    private String staffNum;

    @Schema(description = "所属机构编码", example = "ORG001")
    private String orgCode;

    @Schema(description = "状态", example = "ACTIVE")
    private String status;
}
