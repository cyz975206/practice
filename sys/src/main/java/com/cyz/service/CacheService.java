package com.cyz.service;

import com.cyz.dto.response.CacheStatsResponse;
import com.cyz.dto.response.OnlineUserResponse;

import java.util.List;

public interface CacheService {

    List<OnlineUserResponse> getOnlineUsers();

    void kickUser(Long userId);

    CacheStatsResponse getCacheStats();

    void clearCache(String type);
}
