package com.cyz.service;

import com.cyz.dto.request.TaskLogQueryRequest;
import com.cyz.dto.response.TaskLogResponse;
import org.springframework.data.domain.Page;

public interface SysTaskLogService {

    Page<TaskLogResponse> list(TaskLogQueryRequest request);
}
