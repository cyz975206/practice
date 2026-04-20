package com.cyz.task;

import com.cyz.entity.*;
import com.cyz.repository.*;
import com.cyz.task.helper.TaskLogHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogArchiveTask {

    private final SysConfigRepository configRepository;
    private final SysOperationLogRepository operationLogRepository;
    private final SysOperationLogArchiveRepository operationLogArchiveRepository;
    private final SysLoginLogRepository loginLogRepository;
    private final SysLoginLogArchiveRepository loginLogArchiveRepository;
    private final SysSystemLogRepository systemLogRepository;
    private final SysSystemLogArchiveRepository systemLogArchiveRepository;
    private final SysSecurityLogRepository securityLogRepository;
    private final SysSecurityLogArchiveRepository securityLogArchiveRepository;
    private final TaskLogHelper taskLogHelper;

    private static final int DEFAULT_ARCHIVE_DAYS = 60;

    @Transactional
    public void archiveLogs() {
        taskLogHelper.execute(() -> {
            int operationDays = getConfigDays("operation_log_retention_days");
            int loginDays = getConfigDays("login_log_retention_days");
            int securityDays = getConfigDays("security_log_retention_days");
            int systemDays = getConfigDays("system_log_retention_days");

            int operationCount = archiveOperationLogs(LocalDateTime.now().minusDays(operationDays));
            int loginCount = archiveLoginLogs(LocalDateTime.now().minusDays(loginDays));
            int systemCount = archiveSystemLogs(LocalDateTime.now().minusDays(systemDays));
            int securityCount = archiveSecurityLogs(LocalDateTime.now().minusDays(securityDays));

            return String.format("归档天数: 操作=%d, 登录=%d, 安全=%d, 系统=%d | 归档数量: 操作=%d, 登录=%d, 系统=%d, 安全=%d",
                    operationDays, loginDays, securityDays, systemDays,
                    operationCount, loginCount, systemCount, securityCount);
        });
    }

    private int getConfigDays(String configKey) {
        return configRepository.findByConfigKeyAndIsDeletedFalse(configKey)
                .map(config -> {
                    try {
                        return Integer.parseInt(config.getConfigValue());
                    } catch (NumberFormatException e) {
                        return DEFAULT_ARCHIVE_DAYS;
                    }
                })
                .orElse(DEFAULT_ARCHIVE_DAYS);
    }

    private int archiveOperationLogs(LocalDateTime threshold) {
        java.util.List<SysOperationLog> logs = operationLogRepository.findByIsDeletedFalseAndOperationTimeBefore(threshold);
        if (logs.isEmpty()) return 0;
        for (SysOperationLog log : logs) {
            SysOperationLogArchive archive = new SysOperationLogArchive();
            archive.setOperatorId(log.getOperatorId());
            archive.setOperatorName(log.getOperatorName());
            archive.setOperatorIp(log.getOperatorIp());
            archive.setOperatorOrgCode(log.getOperatorOrgCode());
            archive.setOperatorOrg(log.getOperatorOrg());
            archive.setModule(log.getModule());
            archive.setOperationType(log.getOperationType());
            archive.setOperationContent(log.getOperationContent());
            archive.setRequestParams(log.getRequestParams());
            archive.setOperationResult(log.getOperationResult());
            archive.setErrorMsg(log.getErrorMsg());
            archive.setOperationTime(log.getOperationTime());
            archive.setIsDeleted(false);
            archive.setCreateBy(log.getCreateBy());
            archive.setCreateTime(log.getCreateTime());
            archive.setUpdateTime(log.getUpdateTime());
            operationLogArchiveRepository.save(archive);
        }
        operationLogRepository.deleteAllInBatch(logs);
        return logs.size();
    }

    private int archiveLoginLogs(LocalDateTime threshold) {
        java.util.List<SysLoginLog> logs = loginLogRepository.findByIsDeletedFalseAndLoginTimeBefore(threshold);
        if (logs.isEmpty()) return 0;
        for (SysLoginLog log : logs) {
            SysLoginLogArchive archive = new SysLoginLogArchive();
            archive.setUserId(log.getUserId());
            archive.setUsername(log.getUsername());
            archive.setLoginIp(log.getLoginIp());
            archive.setLoginDevice(log.getLoginDevice());
            archive.setLoginType(log.getLoginType());
            archive.setLoginResult(log.getLoginResult());
            archive.setFailReason(log.getFailReason());
            archive.setLoginTime(log.getLoginTime());
            archive.setLogoutTime(log.getLogoutTime());
            archive.setIsAbnormal(log.getIsAbnormal());
            archive.setIsDeleted(false);
            archive.setCreateBy(log.getCreateBy());
            archive.setCreateTime(log.getCreateTime());
            archive.setUpdateTime(log.getUpdateTime());
            loginLogArchiveRepository.save(archive);
        }
        loginLogRepository.deleteAllInBatch(logs);
        return logs.size();
    }

    private int archiveSystemLogs(LocalDateTime threshold) {
        java.util.List<SysSystemLog> logs = systemLogRepository.findByIsDeletedFalseAndOccurTimeBefore(threshold);
        if (logs.isEmpty()) return 0;
        for (SysSystemLog log : logs) {
            SysSystemLogArchive archive = new SysSystemLogArchive();
            archive.setLogLevel(log.getLogLevel());
            archive.setLogModule(log.getLogModule());
            archive.setLogContent(log.getLogContent());
            archive.setExceptionStack(log.getExceptionStack());
            archive.setOccurTime(log.getOccurTime());
            archive.setServerIp(log.getServerIp());
            archive.setIsDeleted(false);
            archive.setCreateBy(log.getCreateBy());
            archive.setCreateTime(log.getCreateTime());
            archive.setUpdateTime(log.getUpdateTime());
            systemLogArchiveRepository.save(archive);
        }
        systemLogRepository.deleteAllInBatch(logs);
        return logs.size();
    }

    private int archiveSecurityLogs(LocalDateTime threshold) {
        java.util.List<SysSecurityLog> logs = securityLogRepository.findByIsDeletedFalseAndOccurTimeBefore(threshold);
        if (logs.isEmpty()) return 0;
        for (SysSecurityLog log : logs) {
            SysSecurityLogArchive archive = new SysSecurityLogArchive();
            archive.setOperatorId(log.getOperatorId());
            archive.setOperatorName(log.getOperatorName());
            archive.setOperatorIp(log.getOperatorIp());
            archive.setRiskType(log.getRiskType());
            archive.setRiskContent(log.getRiskContent());
            archive.setRequestUrl(log.getRequestUrl());
            archive.setRiskLevel(log.getRiskLevel());
            archive.setHandleStatus(log.getHandleStatus());
            archive.setHandleUserId(log.getHandleUserId());
            archive.setHandleTime(log.getHandleTime());
            archive.setHandleNote(log.getHandleNote());
            archive.setOccurTime(log.getOccurTime());
            archive.setIsDeleted(false);
            archive.setCreateBy(log.getCreateBy());
            archive.setCreateTime(log.getCreateTime());
            archive.setUpdateTime(log.getUpdateTime());
            securityLogArchiveRepository.save(archive);
        }
        securityLogRepository.deleteAllInBatch(logs);
        return logs.size();
    }
}
