package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "人员信息")
public class PersonResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "姓")
    private String surname;

    @Schema(description = "姓拼音")
    private String surnamePinyin;

    @Schema(description = "名")
    private String givenName;

    @Schema(description = "名拼音")
    private String givenNamePinyin;

    @Schema(description = "姓名")
    private String fullName;

    @Schema(description = "姓名拼音")
    private String fullNamePinyin;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "员工工号")
    private String staffNum;

    @Schema(description = "所属机构编码")
    private String orgCode;

    @Schema(description = "所属机构名称")
    private String orgName;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
