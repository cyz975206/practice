package com.cyz.service.impl;

import com.cyz.common.constant.CacheConstant;
import com.cyz.common.exception.BizException;
import com.cyz.dto.response.CacheStatsResponse;
import com.cyz.dto.response.OnlineUserResponse;
import com.cyz.security.LoginUser;
import com.cyz.service.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public List<OnlineUserResponse> getOnlineUsers() {
        Set<String> keys = redisTemplate.keys(CacheConstant.ONLINE_USER_PREFIX + "*");
        List<OnlineUserResponse> result = new ArrayList<>();

        if (keys == null || keys.isEmpty()) {
            return result;
        }

        for (String key : keys) {
            try {
                Object cached = redisTemplate.opsForValue().get(key);
                if (cached == null) {
                    continue;
                }
                LoginUser loginUser;
                if (cached instanceof LoginUser) {
                    loginUser = (LoginUser) cached;
                } else {
                    String json = objectMapper.writeValueAsString(cached);
                    loginUser = objectMapper.readValue(json, LoginUser.class);
                }
                result.add(OnlineUserResponse.builder()
                        .userId(loginUser.getUserId())
                        .username(loginUser.getUsername())
                        .nickname(loginUser.getNickname())
                        .orgCode(loginUser.getOrgCode())
                        .loginTime(loginUser.getLoginTime())
                        .clientIp(loginUser.getClientIp())
                        .build());
            } catch (Exception e) {
                // skip invalid entry
            }
        }
        return result;
    }

    @Override
    public void kickUser(Long userId) {
        String key = CacheConstant.ONLINE_USER_PREFIX + userId;
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.FALSE.equals(deleted)) {
            throw new BizException("用户不在线: " + userId);
        }
    }

    @Override
    public CacheStatsResponse getCacheStats() {
        Set<String> allKeys = redisTemplate.keys("*");
        long keyCount = allKeys != null ? allKeys.size() : 0;

        Set<String> onlineKeys = redisTemplate.keys(CacheConstant.ONLINE_USER_PREFIX + "*");
        long onlineUserCount = onlineKeys != null ? onlineKeys.size() : 0;

        return CacheStatsResponse.builder()
                .keyCount(keyCount)
                .onlineUserCount(onlineUserCount)
                .build();
    }

    @Override
    public void clearCache(String type) {
        switch (type) {
            case "dict" -> deleteByPrefix(CacheConstant.DICT_PREFIX);
            case "menu" -> deleteByPrefix(CacheConstant.MENU_TREE);
            case "config" -> {
                deleteByPrefix(CacheConstant.CONFIG_PREFIX);
                redisTemplate.delete(CacheConstant.JWT_WHITELIST);
            }
            case "perms" -> deleteByPrefix(CacheConstant.PERMS_PREFIX);
            case "all" -> {
                Set<String> keys = redisTemplate.keys("*");
                if (keys != null && !keys.isEmpty()) {
                    redisTemplate.delete(keys);
                }
            }
            default -> throw new BizException("不支持的缓存类型: " + type);
        }
    }

    private void deleteByPrefix(String prefix) {
        Set<String> keys = redisTemplate.keys(prefix + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }
}
