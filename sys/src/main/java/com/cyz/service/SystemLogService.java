package com.cyz.service;

import com.cyz.dto.request.SystemLogQueryRequest;
import com.cyz.dto.response.SystemLogResponse;
import com.cyz.entity.SysSystemLog;
import org.springframework.data.domain.Page;

public interface SystemLogService {

    void save(SysSystemLog log);

    Page<SystemLogResponse> list(SystemLogQueryRequest request);
}
