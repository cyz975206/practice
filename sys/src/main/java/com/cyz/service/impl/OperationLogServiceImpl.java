package com.cyz.service.impl;

import com.cyz.dto.request.OperationLogQueryRequest;
import com.cyz.dto.response.OperationLogResponse;
import com.cyz.entity.SysOperationLog;
import com.cyz.repository.SysOperationLogRepository;
import com.cyz.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OperationLogServiceImpl implements OperationLogService {

    private final SysOperationLogRepository repository;

    @Async
    @Override
    public void save(SysOperationLog log) {
        repository.save(log);
    }

    @Override
    public Page<OperationLogResponse> list(OperationLogQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<SysOperationLog> page = repository.findByConditions(
                request.getOperatorName(), request.getModule(), request.getOperationType(),
                request.getResult(), request.getStartTime(), request.getEndTime(), pageRequest);
        return page.map(this::toResponse);
    }

    private OperationLogResponse toResponse(SysOperationLog entity) {
        OperationLogResponse r = new OperationLogResponse();
        r.setId(entity.getId());
        r.setOperatorId(entity.getOperatorId());
        r.setOperatorName(entity.getOperatorName());
        r.setOperatorIp(entity.getOperatorIp());
        r.setOperatorOrgCode(entity.getOperatorOrgCode());
        r.setOperatorOrg(entity.getOperatorOrg());
        r.setModule(entity.getModule());
        r.setOperationType(entity.getOperationType());
        r.setOperationContent(entity.getOperationContent());
        r.setRequestParams(entity.getRequestParams());
        r.setOperationResult(entity.getOperationResult());
        r.setErrorMsg(entity.getErrorMsg());
        r.setOperationTime(entity.getOperationTime());
        r.setCreateTime(entity.getCreateTime());
        return r;
    }
}
