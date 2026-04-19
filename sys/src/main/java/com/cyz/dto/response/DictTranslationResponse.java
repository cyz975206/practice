package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "字典翻译结果")
public class DictTranslationResponse {

    @Schema(description = "字典类型编码")
    private String dictType;

    @Schema(description = "翻译项列表")
    private List<TranslationItem> translations;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "翻译项")
    public static class TranslationItem {

        @Schema(description = "字典项编码")
        private String code;

        @Schema(description = "字典项显示名")
        private String label;

        @Schema(description = "字典项值")
        private String value;
    }
}
