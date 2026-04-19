package com.cyz.service.impl;

import com.cyz.common.constant.CacheConstant;
import com.cyz.common.exception.BizException;
import com.cyz.converter.ConfigConverter;
import com.cyz.dto.request.ConfigCreateRequest;
import com.cyz.dto.request.ConfigQueryRequest;
import com.cyz.dto.request.ConfigUpdateRequest;
import com.cyz.dto.response.ConfigResponse;
import com.cyz.entity.SysConfig;
import com.cyz.repository.SysConfigRepository;
import com.cyz.service.ConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ConfigServiceImpl implements ConfigService {

    private final SysConfigRepository configRepository;
    private final ConfigConverter configConverter;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public ConfigResponse create(ConfigCreateRequest request) {
        if (configRepository.existsByConfigKeyAndIsDeletedFalse(request.getConfigKey())) {
            throw new BizException("配置键已存在: " + request.getConfigKey());
        }

        SysConfig config = configConverter.toEntity(request);
        SysConfig saved = configRepository.save(config);
        return configConverter.toResponse(saved);
    }

    @Override
    @Transactional
    public ConfigResponse update(Long id, ConfigUpdateRequest request) {
        SysConfig config = configRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("配置不存在: " + id));

        configConverter.updateEntity(config, request);
        SysConfig saved = configRepository.save(config);

        redisTemplate.delete(CacheConstant.CONFIG_PREFIX + config.getConfigKey());

        return configConverter.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysConfig config = configRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("配置不存在: " + id));

        config.setIsDeleted(true);
        configRepository.save(config);

        redisTemplate.delete(CacheConstant.CONFIG_PREFIX + config.getConfigKey());
    }

    @Override
    public ConfigResponse getById(Long id) {
        SysConfig config = configRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("配置不存在: " + id));
        return configConverter.toResponse(config);
    }

    @Override
    public Page<ConfigResponse> list(ConfigQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<SysConfig> page = configRepository.findByConditions(
                request.getConfigKey(), request.getConfigName(), pageRequest);
        return page.map(configConverter::toResponse);
    }

    @Override
    public String getByKey(String configKey) {
        String cacheKey = CacheConstant.CONFIG_PREFIX + configKey;
        Object cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached.toString();
        }

        SysConfig config = configRepository.findByConfigKeyAndIsDeletedFalse(configKey)
                .orElseThrow(() -> new BizException("配置不存在: " + configKey));

        long expireMinutes = getCacheExpireMinutes();
        redisTemplate.opsForValue().set(cacheKey, config.getConfigValue(), expireMinutes, TimeUnit.MINUTES);

        return config.getConfigValue();
    }

    private long getCacheExpireMinutes() {
        int cacheExpireHour = getConfigIntDirect("cache_expire_hour", 1);
        int randomOffset = getConfigIntDirect("cache_random_offset_minute", 10);
        long baseMinutes = cacheExpireHour * 60L;
        long offset = ThreadLocalRandom.current().nextLong(0, randomOffset + 1);
        return baseMinutes + offset;
    }

    private int getConfigIntDirect(String key, int defaultValue) {
        return configRepository.findByConfigKeyAndIsDeletedFalse(key)
                .map(c -> {
                    try {
                        return Integer.parseInt(c.getConfigValue());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }
}
