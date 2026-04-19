package com.cyz.converter;

import com.cyz.dto.request.TaskCreateRequest;
import com.cyz.dto.request.TaskUpdateRequest;
import com.cyz.dto.response.TaskResponse;
import com.cyz.entity.SysTask;
import org.springframework.stereotype.Component;

@Component
public class TaskConverter {

    public SysTask toEntity(TaskCreateRequest request) {
        return SysTask.builder()
                .name(request.getName())
                .serviceName(request.getServiceName())
                .funPath(request.getFunPath())
                .cron(request.getCron())
                .hasStart(request.getHasStart() != null ? request.getHasStart() : false)
                .build();
    }

    public void updateEntity(SysTask entity, TaskUpdateRequest request) {
        if (request.getName() != null) {
            entity.setName(request.getName());
        }
        if (request.getFunPath() != null) {
            entity.setFunPath(request.getFunPath());
        }
        if (request.getCron() != null) {
            entity.setCron(request.getCron());
        }
        if (request.getHasStart() != null) {
            entity.setHasStart(request.getHasStart());
        }
    }

    public TaskResponse toResponse(SysTask entity) {
        TaskResponse response = new TaskResponse();
        response.setId(entity.getId());
        response.setName(entity.getName());
        response.setServiceName(entity.getServiceName());
        response.setFunPath(entity.getFunPath());
        response.setCron(entity.getCron());
        response.setHasStart(entity.getHasStart());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());
        return response;
    }
}
