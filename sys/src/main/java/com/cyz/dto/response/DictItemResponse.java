package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "字典项信息")
public class DictItemResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "字典类型编码")
    private String dictType;

    @Schema(description = "字典项编码")
    private String dictCode;

    @Schema(description = "字典项显示名")
    private String dictLabel;

    @Schema(description = "字典项值")
    private String dictValue;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
