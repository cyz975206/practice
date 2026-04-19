package com.cyz.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建人员请求")
public class PersonCreateRequest {

    @NotBlank(message = "姓不能为空")
    @Schema(description = "姓", example = "张", requiredMode = Schema.RequiredMode.REQUIRED)
    private String surname;

    @NotBlank(message = "名不能为空")
    @Schema(description = "名", example = "三", requiredMode = Schema.RequiredMode.REQUIRED)
    private String givenName;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "手机号")
    private String phone;

    @NotBlank(message = "员工工号不能为空")
    @Schema(description = "员工工号", example = "EMP001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String staffNum;

    @NotBlank(message = "所属机构号不能为空")
    @Schema(description = "所属机构编码", example = "ORG001", requiredMode = Schema.RequiredMode.REQUIRED)
    private String orgCode;

    @Schema(description = "状态", example = "ACTIVE")
    private String status;
}
