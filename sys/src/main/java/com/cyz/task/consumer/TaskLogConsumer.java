package com.cyz.task.consumer;

import com.cyz.common.constant.CacheConstant;
import com.cyz.dto.SysTaskLogDTO;
import com.cyz.entity.SysTaskLog;
import com.cyz.repository.SysTaskLogRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskLogConsumer {

    private final RedisTemplate<String, Object> redisTemplate;
    private final SysTaskLogRepository taskLogRepository;

    private volatile boolean running = true;

    @PostConstruct
    public void start() {
        Thread consumerThread = new Thread(this::consumeLoop, "task-log-consumer");
        consumerThread.setDaemon(true);
        consumerThread.start();
        log.info("任务日志消费者已启动");
    }

    @PreDestroy
    public void stop() {
        running = false;
        drainRemaining();
        log.info("任务日志消费者已停止");
    }

    /**
     * 应用关闭时尝试消费剩余消息，减少消息丢失
     */
    private void drainRemaining() {
        int count = 0;
        while (count < 100) { // 最多消费100条，避免阻塞关闭
            try {
                Object result = redisTemplate.opsForList().rightPop(CacheConstant.TASK_LOG_QUEUE, 1, TimeUnit.SECONDS);
                if (result == null) break;
                SysTaskLogDTO dto;
                if (result instanceof SysTaskLogDTO) {
                    dto = (SysTaskLogDTO) result;
                } else if (result instanceof Map) {
                    dto = convertFromMap((Map<String, Object>) result);
                } else {
                    continue;
                }
                saveLog(dto);
                count++;
            } catch (Exception e) {
                break;
            }
        }
        if (count > 0) {
            log.info("关闭前消费了 {} 条剩余任务日志", count);
        }
    }

    @SuppressWarnings("unchecked")
    private void consumeLoop() {
        while (running) {
            try {
                Object result = redisTemplate.opsForList().rightPop(
                        CacheConstant.TASK_LOG_QUEUE, 30, TimeUnit.SECONDS);
                if (result != null) {
                    SysTaskLogDTO dto;
                    if (result instanceof SysTaskLogDTO) {
                        dto = (SysTaskLogDTO) result;
                    } else if (result instanceof Map) {
                        dto = convertFromMap((Map<String, Object>) result);
                    } else {
                        log.warn("未知类型的任务日志消息: {}", result.getClass());
                        continue;
                    }
                    saveLog(dto);
                }
            } catch (Exception e) {
                if (running) {
                    log.error("任务日志消费异常", e);
                }
            }
        }
    }

    private void saveLog(SysTaskLogDTO dto) {
        try {
            SysTaskLog taskLog = new SysTaskLog();
            taskLog.setTaskId(dto.getTaskId());
            taskLog.setTaskName(dto.getTaskName());
            taskLog.setServiceName(dto.getServiceName());
            taskLog.setFunPath(dto.getFunPath());
            taskLog.setCron(dto.getCron());
            taskLog.setRunResult(dto.getRunResult());
            taskLog.setRunLog(dto.getRunLog());
            taskLog.setDurationMs(dto.getDurationMs());
            taskLogRepository.save(taskLog);
        } catch (Exception e) {
            log.error("保存任务日志失败: funPath={}", dto.getFunPath(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private SysTaskLogDTO convertFromMap(Map<String, Object> map) {
        SysTaskLogDTO dto = new SysTaskLogDTO();
        dto.setTaskId(toLong(map.get("taskId")));
        dto.setTaskName(toString(map.get("taskName")));
        dto.setServiceName(toString(map.get("serviceName")));
        dto.setFunPath(toString(map.get("funPath")));
        dto.setCron(toString(map.get("cron")));
        dto.setRunResult(toInteger(map.get("runResult")));
        dto.setRunLog(toString(map.get("runLog")));
        dto.setDurationMs(toLong(map.get("durationMs")));
        return dto;
    }

    private Long toLong(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        try { return Long.parseLong(val.toString()); } catch (NumberFormatException e) { return null; }
    }

    private Integer toInteger(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        try { return Integer.parseInt(val.toString()); } catch (NumberFormatException e) { return null; }
    }

    private String toString(Object val) {
        return val != null ? val.toString() : null;
    }
}
