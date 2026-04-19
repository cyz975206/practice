package com.cyz.service.impl;

import com.cyz.dto.request.SystemLogQueryRequest;
import com.cyz.dto.response.SystemLogResponse;
import com.cyz.entity.SysSystemLog;
import com.cyz.repository.SysSystemLogRepository;
import com.cyz.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private final SysSystemLogRepository repository;

    @Async
    @Override
    public void save(SysSystemLog log) {
        repository.save(log);
    }

    @Override
    public Page<SystemLogResponse> list(SystemLogQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<SysSystemLog> page = repository.findByConditions(
                request.getLogLevel(), request.getLogModule(),
                request.getStartTime(), request.getEndTime(), pageRequest);
        return page.map(this::toResponse);
    }

    private SystemLogResponse toResponse(SysSystemLog entity) {
        SystemLogResponse r = new SystemLogResponse();
        r.setId(entity.getId());
        r.setLogLevel(entity.getLogLevel());
        r.setLogModule(entity.getLogModule());
        r.setLogContent(entity.getLogContent());
        r.setExceptionStack(entity.getExceptionStack());
        r.setOccurTime(entity.getOccurTime());
        r.setServerIp(entity.getServerIp());
        r.setCreateTime(entity.getCreateTime());
        return r;
    }
}
