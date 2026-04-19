package com.cyz.converter;

import com.cyz.dto.request.ConfigCreateRequest;
import com.cyz.dto.request.ConfigUpdateRequest;
import com.cyz.dto.response.ConfigResponse;
import com.cyz.entity.SysConfig;
import org.springframework.stereotype.Component;

@Component
public class ConfigConverter {

    public SysConfig toEntity(ConfigCreateRequest request) {
        return SysConfig.builder()
                .configKey(request.getConfigKey())
                .configName(request.getConfigName())
                .configValue(request.getConfigValue())
                .remark(request.getRemark())
                .sort(request.getSort() != null ? request.getSort() : 0)
                .build();
    }

    public void updateEntity(SysConfig entity, ConfigUpdateRequest request) {
        if (request.getConfigName() != null) {
            entity.setConfigName(request.getConfigName());
        }
        if (request.getConfigValue() != null) {
            entity.setConfigValue(request.getConfigValue());
        }
        if (request.getRemark() != null) {
            entity.setRemark(request.getRemark());
        }
        if (request.getSort() != null) {
            entity.setSort(request.getSort());
        }
    }

    public ConfigResponse toResponse(SysConfig entity) {
        ConfigResponse response = new ConfigResponse();
        response.setId(entity.getId());
        response.setConfigKey(entity.getConfigKey());
        response.setConfigName(entity.getConfigName());
        response.setConfigValue(entity.getConfigValue());
        response.setRemark(entity.getRemark());
        response.setSort(entity.getSort());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());
        return response;
    }
}
