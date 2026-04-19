package com.cyz.service;

import com.cyz.dto.request.OperationLogQueryRequest;
import com.cyz.dto.response.OperationLogResponse;
import com.cyz.entity.SysOperationLog;
import org.springframework.data.domain.Page;

public interface OperationLogService {

    void save(SysOperationLog log);

    Page<OperationLogResponse> list(OperationLogQueryRequest request);
}
