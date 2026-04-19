package com.cyz.common.aspect;

import com.cyz.common.annotation.OperationLog;
import com.cyz.common.enums.OperationType;
import com.cyz.common.util.IpUtil;
import com.cyz.dto.response.LoginUserResponse;
import com.cyz.entity.SysOperationLog;
import com.cyz.security.LoginUser;
import com.cyz.security.LoginUserContext;
import com.cyz.service.DictService;
import com.cyz.service.OperationLogService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class OperationLogAspect {

    private final OperationLogService operationLogService;
    private final DictService dictService;
    private final ObjectMapper objectMapper;

    @Around("@annotation(operationLog)")
    public Object around(ProceedingJoinPoint point, OperationLog operationLog) throws Throwable {
        SysOperationLog logEntity = new SysOperationLog();
        logEntity.setModule(operationLog.module());
        logEntity.setOperationType(dictService.getDictValue(OperationType.class.getSimpleName(), operationLog.type().name()));
        logEntity.setOperationContent(operationLog.description());
        logEntity.setOperationTime(LocalDateTime.now());

        // 获取操作人信息
        LoginUser loginUser = LoginUserContext.get();
        if (loginUser != null) {
            logEntity.setOperatorId(loginUser.getUserId());
            logEntity.setOperatorName(loginUser.getNickname() != null ? loginUser.getNickname() : loginUser.getUsername());
            logEntity.setOperatorOrgCode(loginUser.getOrgCode());
        }

        // 获取请求 IP
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            logEntity.setOperatorIp(IpUtil.getClientIp(request));
        }

        // 记录请求参数
        try {
            Object[] args = point.getArgs();
            if (args != null && args.length > 0) {
                Object arg = args[0];
                if (arg != null) {
                    logEntity.setRequestParams(objectMapper.writeValueAsString(arg));
                }
            }
        } catch (Exception e) {
            log.warn("序列化请求参数失败", e);
        }

        // 执行目标方法
        try {
            Object result = point.proceed();
            logEntity.setOperationResult("success");
            return result;
        } catch (Throwable e) {
            logEntity.setOperationResult("fail");
            logEntity.setErrorMsg(e.getMessage());
            throw e;
        } finally {
            operationLogService.save(logEntity);
        }
    }
}
