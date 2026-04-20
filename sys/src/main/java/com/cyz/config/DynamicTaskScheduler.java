package com.cyz.config;

import com.cyz.common.constant.CacheConstant;
import com.cyz.common.context.TaskLogContext;
import com.cyz.common.exception.BizException;
import com.cyz.dto.SysTaskLogDTO;
import com.cyz.entity.SysTask;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicTaskScheduler {

    private final ApplicationContext applicationContext;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final RedissonClient redissonClient;
    private final TaskProperties taskProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    private final Map<String, ScheduledFuture<?>> runningTasks = new ConcurrentHashMap<>();

    /**
     * 解析funPath（格式: beanName.methodName）为Runnable
     */
    public Runnable resolveTask(SysTask task) {
        String funPath = task.getFunPath();
        String[] parts = funPath.split("\\.");
        if (parts.length != 2) {
            throw new BizException("方法路径格式错误，应为 beanName.methodName: " + funPath);
        }
        String beanName = parts[0];
        String methodName = parts[1];

        Object bean;
        try {
            bean = applicationContext.getBean(beanName);
        } catch (Exception e) {
            throw new BizException("未找到Spring Bean: " + beanName);
        }

        Method method;
        try {
            method = bean.getClass().getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new BizException("未找到方法: " + methodName + " (在Bean: " + beanName + "中)");
        }

        return () -> executeWithLock(funPath, bean, method, task);
    }

    /**
     * 分布式锁保护的任务执行
     */
    private void executeWithLock(String funPath, Object bean, Method method, SysTask task) {
        String lockKey = CacheConstant.TASK_LOCK + ":" + taskProperties.getServiceName() + ":exec:" + funPath;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean locked = lock.tryLock(0, 60, TimeUnit.SECONDS);
            if (!locked) {
                log.debug("任务已被其他实例执行，跳过: {}", funPath);
                return;
            }
            try {
                log.debug("动态任务开始执行: {}", funPath);

                LocalDateTime startTime = LocalDateTime.now();
                TaskLogContext.TaskLogInfo contextInfo = TaskLogContext.TaskLogInfo.builder()
                        .taskId(task.getId())
                        .taskName(task.getName())
                        .serviceName(task.getServiceName())
                        .funPath(task.getFunPath())
                        .cron(task.getCron())
                        .startTime(startTime)
                        .logged(false)
                        .build();
                TaskLogContext.set(contextInfo);

                boolean success = true;
                String errorMsg = null;
                try {
                    method.invoke(bean);
                } catch (InvocationTargetException e) {
                    success = false;
                    Throwable cause = e.getTargetException();
                    errorMsg = cause.getClass().getSimpleName() + ": " + cause.getMessage();
                    if (cause instanceof RuntimeException re) {
                        throw re;
                    } else if (cause instanceof Error err) {
                        throw err;
                    } else {
                        throw new RuntimeException(cause);
                    }
                } finally {
                    autoLogIfNeeded(contextInfo, success, errorMsg);
                    TaskLogContext.clear();
                }
                log.debug("动态任务执行完成: {}", funPath);
            } finally {
                lock.unlock();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("获取任务执行锁被中断: {}", funPath);
        } catch (Exception e) {
            log.error("动态任务执行失败: {}", funPath, e);
        }
    }

    /**
     * 如果任务方法未使用 TaskLogHelper，自动写入基础日志
     */
    private void autoLogIfNeeded(TaskLogContext.TaskLogInfo contextInfo, boolean success, String errorMsg) {
        if (contextInfo.isLogged()) {
            return;
        }
        long durationMs = Duration.between(contextInfo.getStartTime(), LocalDateTime.now()).toMillis();
        String runLog;
        if (success) {
            runLog = "任务执行完成";
        } else {
            runLog = errorMsg != null ? "异常信息: " + errorMsg : "任务执行失败";
        }
        SysTaskLogDTO dto = new SysTaskLogDTO();
        dto.setTaskId(contextInfo.getTaskId());
        dto.setTaskName(contextInfo.getTaskName());
        dto.setServiceName(contextInfo.getServiceName());
        dto.setFunPath(contextInfo.getFunPath());
        dto.setCron(contextInfo.getCron());
        dto.setRunResult(success ? 1 : 0);
        dto.setRunLog(runLog);
        dto.setDurationMs(durationMs);
        try {
            redisTemplate.opsForList().leftPush(CacheConstant.TASK_LOG_QUEUE, dto);
        } catch (Exception e) {
            log.error("推送任务自动日志到队列失败: funPath={}", contextInfo.getFunPath(), e);
        }
    }

    /**
     * 注册定时任务
     */
    public synchronized void addTask(SysTask task) {
        removeTask(task.getFunPath());
        Runnable runnable = resolveTask(task);
        try {
            CronTrigger trigger = new CronTrigger(task.getCron());
            ScheduledFuture<?> future = taskScheduler.schedule(runnable, trigger);
            runningTasks.put(task.getFunPath(), future);
            log.info("动态任务已注册: {} [{}]", task.getName(), task.getCron());
        } catch (IllegalArgumentException e) {
            throw new BizException("Cron表达式无效: " + task.getCron());
        }
    }

    /**
     * 取消定时任务
     */
    public synchronized void removeTask(String funPath) {
        ScheduledFuture<?> future = runningTasks.remove(funPath);
        if (future != null) {
            future.cancel(false);
            log.info("动态任务已取消: {}", funPath);
        }
    }

    /**
     * 手动触发一次（异步执行，分布式锁保护）
     */
    public void executeTaskNow(SysTask task) {
        Runnable runnable = resolveTask(task);
        taskScheduler.execute(runnable);
    }

    /**
     * 启动时批量注册已启用任务
     */
    public void initAllTasks(List<SysTask> tasks) {
        for (SysTask task : tasks) {
            if (Boolean.TRUE.equals(task.getHasStart())) {
                try {
                    addTask(task);
                } catch (Exception e) {
                    log.error("初始化任务失败: {} [{}]", task.getName(), task.getFunPath(), e);
                }
            }
        }
    }

    /**
     * 全量刷新：取消全部 + 重新注册已启用的
     */
    public synchronized void refreshTasks(List<SysTask> tasks) {
        for (String funPath : runningTasks.keySet()) {
            ScheduledFuture<?> future = runningTasks.get(funPath);
            if (future != null) {
                future.cancel(false);
            }
        }
        runningTasks.clear();

        for (SysTask task : tasks) {
            if (Boolean.TRUE.equals(task.getHasStart())) {
                try {
                    addTask(task);
                } catch (Exception e) {
                    log.error("刷新任务失败: {} [{}]", task.getName(), task.getFunPath(), e);
                }
            }
        }
        log.info("定时任务刷新完成，共注册 {} 个已启用任务",
                tasks.stream().filter(t -> Boolean.TRUE.equals(t.getHasStart())).count());
    }

    @PreDestroy
    public void shutdown() {
        for (Map.Entry<String, ScheduledFuture<?>> entry : runningTasks.entrySet()) {
            entry.getValue().cancel(false);
        }
        runningTasks.clear();
        log.info("所有动态任务已停止");
    }
}
