package com.cyz.service;

import com.cyz.dto.request.SecurityLogQueryRequest;
import com.cyz.dto.response.SecurityLogResponse;
import com.cyz.entity.SysSecurityLog;
import org.springframework.data.domain.Page;

public interface SecurityLogService {

    void save(SysSecurityLog log);

    Page<SecurityLogResponse> list(SecurityLogQueryRequest request);

    void handle(Long id, String handleStatus, String handleNote);
}
