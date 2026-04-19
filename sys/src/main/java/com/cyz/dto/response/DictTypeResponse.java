package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "字典类型信息")
public class DictTypeResponse {

    @Schema(description = "ID")
    private Long id;

    @Schema(description = "字典类型编码")
    private String dictType;

    @Schema(description = "字典类型名称")
    private String dictName;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "字典项数量")
    private Integer itemCount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
