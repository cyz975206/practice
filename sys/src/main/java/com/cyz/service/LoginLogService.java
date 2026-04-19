package com.cyz.service;

import com.cyz.dto.request.LoginLogQueryRequest;
import com.cyz.dto.response.LoginLogResponse;
import com.cyz.entity.SysLoginLog;
import org.springframework.data.domain.Page;

public interface LoginLogService {

    void save(SysLoginLog log);

    Page<LoginLogResponse> list(LoginLogQueryRequest request);
}
