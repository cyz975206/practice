package com.cyz.task.helper;

import com.cyz.common.constant.CacheConstant;
import com.cyz.common.context.TaskLogContext;
import com.cyz.dto.SysTaskLogDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class TaskLogHelper {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 执行任务逻辑并自动记录日志
     *
     * @param taskLogic 任务逻辑，返回自定义日志内容
     */
    public void execute(Supplier<String> taskLogic) {
        TaskLogContext.TaskLogInfo ctx = TaskLogContext.get();
        if (ctx == null) {
            taskLogic.get();
            return;
        }

        LocalDateTime startTime = ctx.getStartTime();
        LocalDateTime endTime = null;
        int runResult = 1;
        String customLog = null;
        String errorMsg = null;

        try {
            customLog = taskLogic.get();
            endTime = LocalDateTime.now();
        } catch (Throwable e) {
            runResult = 0;
            errorMsg = e.getClass().getSimpleName() + ": " + e.getMessage();
            endTime = LocalDateTime.now();
            if (e instanceof RuntimeException re) {
                throw re;
            } else if (e instanceof Error err) {
                throw err;
            } else {
                throw new RuntimeException(e);
            }
        } finally {
            long durationMs = Duration.between(startTime, endTime != null ? endTime : LocalDateTime.now()).toMillis();
            StringBuilder sb = new StringBuilder();
            sb.append("开始时间: ").append(startTime.format(FMT));
            if (customLog != null) {
                sb.append("\n执行详情: ").append(customLog);
            }
            if (errorMsg != null) {
                sb.append("\n异常信息: ").append(errorMsg);
            }
            sb.append("\n结束时间: ").append((endTime != null ? endTime : LocalDateTime.now()).format(FMT));
            sb.append("\n执行耗时: ").append(durationMs).append("ms");

            SysTaskLogDTO dto = new SysTaskLogDTO();
            dto.setTaskId(ctx.getTaskId());
            dto.setTaskName(ctx.getTaskName());
            dto.setServiceName(ctx.getServiceName());
            dto.setFunPath(ctx.getFunPath());
            dto.setCron(ctx.getCron());
            dto.setRunResult(runResult);
            dto.setRunLog(sb.toString());
            dto.setDurationMs(durationMs);

            pushToQueue(dto);
            ctx.setLogged(true);
        }
    }

    private void pushToQueue(SysTaskLogDTO dto) {
        try {
            redisTemplate.opsForList().leftPush(CacheConstant.TASK_LOG_QUEUE, dto);
        } catch (Exception e) {
            log.error("推送任务日志到队列失败: funPath={}", dto.getFunPath(), e);
        }
    }
}
