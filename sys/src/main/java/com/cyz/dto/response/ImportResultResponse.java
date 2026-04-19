package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "导入结果")
public class ImportResultResponse {

    @Schema(description = "总条数")
    private int totalCount;

    @Schema(description = "成功条数")
    private int successCount;

    @Schema(description = "失败条数")
    private int failCount;

    @Builder.Default
    @Schema(description = "错误信息列表")
    private List<String> errors = new ArrayList<>();
}
