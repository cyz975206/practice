package com.cyz.service;

import com.cyz.dto.request.TaskCreateRequest;
import com.cyz.dto.request.TaskQueryRequest;
import com.cyz.dto.request.TaskUpdateRequest;
import com.cyz.dto.response.TaskResponse;
import org.springframework.data.domain.Page;

public interface SysTaskService {

    TaskResponse create(TaskCreateRequest request);

    TaskResponse update(Long id, TaskUpdateRequest request);

    void delete(Long id);

    TaskResponse getById(Long id);

    Page<TaskResponse> list(TaskQueryRequest request);

    void start(Long id);

    void stop(Long id);

    void trigger(Long id);
}
