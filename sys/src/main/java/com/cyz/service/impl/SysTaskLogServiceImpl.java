package com.cyz.service.impl;

import com.cyz.dto.request.TaskLogQueryRequest;
import com.cyz.dto.response.TaskLogResponse;
import com.cyz.entity.SysTaskLog;
import com.cyz.repository.SysTaskLogRepository;
import com.cyz.service.SysTaskLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SysTaskLogServiceImpl implements SysTaskLogService {

    private final SysTaskLogRepository repository;

    @Override
    public Page<TaskLogResponse> list(TaskLogQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<SysTaskLog> page = repository.findByConditions(
                request.getTaskName(), request.getFunPath(), request.getRunResult(),
                request.getStartTime(), request.getEndTime(), pageRequest);
        return page.map(this::toResponse);
    }

    private TaskLogResponse toResponse(SysTaskLog entity) {
        TaskLogResponse r = new TaskLogResponse();
        r.setId(entity.getId());
        r.setTaskId(entity.getTaskId());
        r.setTaskName(entity.getTaskName());
        r.setServiceName(entity.getServiceName());
        r.setFunPath(entity.getFunPath());
        r.setCron(entity.getCron());
        r.setRunResult(entity.getRunResult());
        r.setRunLog(entity.getRunLog());
        r.setDurationMs(entity.getDurationMs());
        r.setCreateTime(entity.getCreateTime());
        return r;
    }
}
