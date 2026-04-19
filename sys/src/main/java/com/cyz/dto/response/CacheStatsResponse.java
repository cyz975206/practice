package com.cyz.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "缓存统计信息")
public class CacheStatsResponse {

    @Schema(description = "缓存Key总数")
    private long keyCount;

    @Schema(description = "在线用户数")
    private long onlineUserCount;
}
