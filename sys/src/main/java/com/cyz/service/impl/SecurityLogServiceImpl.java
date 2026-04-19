package com.cyz.service.impl;

import com.cyz.common.exception.BizException;
import com.cyz.dto.request.SecurityLogQueryRequest;
import com.cyz.dto.response.SecurityLogResponse;
import com.cyz.entity.SysSecurityLog;
import com.cyz.repository.SysSecurityLogRepository;
import com.cyz.security.LoginUser;
import com.cyz.security.LoginUserContext;
import com.cyz.service.SecurityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SecurityLogServiceImpl implements SecurityLogService {

    private final SysSecurityLogRepository repository;

    @Async
    @Override
    public void save(SysSecurityLog log) {
        repository.save(log);
    }

    @Override
    public Page<SecurityLogResponse> list(SecurityLogQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<SysSecurityLog> page = repository.findByConditions(
                request.getRiskType(), request.getRiskLevel(), request.getHandleStatus(),
                request.getStartTime(), request.getEndTime(), pageRequest);
        return page.map(this::toResponse);
    }

    @Override
    @Transactional
    public void handle(Long id, String handleStatus, String handleNote) {
        SysSecurityLog log = repository.findById(id)
                .orElseThrow(() -> new BizException("安全日志不存在: " + id));
        log.setHandleStatus(handleStatus);
        log.setHandleNote(handleNote);
        log.setHandleTime(LocalDateTime.now());
        LoginUser user = LoginUserContext.get();
        if (user != null) {
            log.setHandleUserId(user.getUserId());
        }
        repository.save(log);
    }

    private SecurityLogResponse toResponse(SysSecurityLog entity) {
        SecurityLogResponse r = new SecurityLogResponse();
        r.setId(entity.getId());
        r.setOperatorId(entity.getOperatorId());
        r.setOperatorName(entity.getOperatorName());
        r.setOperatorIp(entity.getOperatorIp());
        r.setRiskType(entity.getRiskType());
        r.setRiskContent(entity.getRiskContent());
        r.setRequestUrl(entity.getRequestUrl());
        r.setRiskLevel(entity.getRiskLevel());
        r.setHandleStatus(entity.getHandleStatus());
        r.setHandleUserId(entity.getHandleUserId());
        r.setHandleTime(entity.getHandleTime());
        r.setHandleNote(entity.getHandleNote());
        r.setOccurTime(entity.getOccurTime());
        r.setCreateTime(entity.getCreateTime());
        return r;
    }
}
