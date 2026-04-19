package com.cyz.service;

import com.cyz.dto.request.ConfigCreateRequest;
import com.cyz.dto.request.ConfigQueryRequest;
import com.cyz.dto.request.ConfigUpdateRequest;
import com.cyz.dto.response.ConfigResponse;
import org.springframework.data.domain.Page;

public interface ConfigService {

    ConfigResponse create(ConfigCreateRequest request);

    ConfigResponse update(Long id, ConfigUpdateRequest request);

    void delete(Long id);

    ConfigResponse getById(Long id);

    Page<ConfigResponse> list(ConfigQueryRequest request);

    String getByKey(String configKey);
}
