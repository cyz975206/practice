package com.cyz.controller;

import com.cyz.common.result.R;
import com.cyz.dto.response.CacheStatsResponse;
import com.cyz.dto.response.OnlineUserResponse;
import com.cyz.service.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "缓存管理")
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final CacheService cacheService;

    @Operation(summary = "获取在线用户列表")
    @GetMapping("/online-users")
    public R<List<OnlineUserResponse>> getOnlineUsers() {
        return R.ok(cacheService.getOnlineUsers());
    }

    @Operation(summary = "强制用户下线")
    @DeleteMapping("/online-users/{userId}")
    public R<Void> kickUser(@Parameter(description = "用户ID") @PathVariable Long userId) {
        cacheService.kickUser(userId);
        return R.ok();
    }

    @Operation(summary = "获取缓存统计信息")
    @GetMapping("/stats")
    public R<CacheStatsResponse> getCacheStats() {
        return R.ok(cacheService.getCacheStats());
    }

    @Operation(summary = "清理指定缓存")
    @DeleteMapping("/clear")
    public R<Void> clearCache(@Parameter(description = "缓存类型（dict/menu/config/perms/all）") @RequestParam String type) {
        cacheService.clearCache(type);
        return R.ok();
    }
}
