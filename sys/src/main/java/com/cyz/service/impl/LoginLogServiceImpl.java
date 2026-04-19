package com.cyz.service.impl;

import com.cyz.dto.request.LoginLogQueryRequest;
import com.cyz.dto.response.LoginLogResponse;
import com.cyz.entity.SysLoginLog;
import com.cyz.repository.SysLoginLogRepository;
import com.cyz.service.LoginLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoginLogServiceImpl implements LoginLogService {

    private final SysLoginLogRepository repository;

    @Async
    @Override
    public void save(SysLoginLog log) {
        repository.save(log);
    }

    @Override
    public Page<LoginLogResponse> list(LoginLogQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<SysLoginLog> page = repository.findByConditions(
                request.getUsername(), request.getLoginType(), request.getLoginResult(),
                request.getStartTime(), request.getEndTime(), pageRequest);
        return page.map(this::toResponse);
    }

    private LoginLogResponse toResponse(SysLoginLog entity) {
        LoginLogResponse r = new LoginLogResponse();
        r.setId(entity.getId());
        r.setUserId(entity.getUserId());
        r.setUsername(entity.getUsername());
        r.setLoginIp(entity.getLoginIp());
        r.setLoginDevice(entity.getLoginDevice());
        r.setLoginType(entity.getLoginType());
        r.setLoginResult(entity.getLoginResult());
        r.setFailReason(entity.getFailReason());
        r.setLoginTime(entity.getLoginTime());
        r.setLogoutTime(entity.getLogoutTime());
        r.setIsAbnormal(entity.getIsAbnormal());
        r.setCreateTime(entity.getCreateTime());
        return r;
    }
}
