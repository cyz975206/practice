package com.cyz.config;

import com.cyz.common.exception.BizException;
import com.cyz.entity.SysTask;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
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

    private final Map<String, ScheduledFuture<?>> runningTasks = new ConcurrentHashMap<>();

    /**
     * 解析funPath（格式: beanName.methodName）为Runnable
     */
    public Runnable resolveTask(String funPath) {
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

        return () -> executeWithLock(funPath, bean, method);
    }

    /**
     * 分布式锁保护的任务执行
     */
    private void executeWithLock(String funPath, Object bean, Method method) {
        String lockKey = com.cyz.common.constant.CacheConstant.TASK_LOCK + ":" + taskProperties.getServiceName() + ":exec:" + funPath;
        RLock lock = redissonClient.getLock(lockKey);
        try {
            boolean locked = lock.tryLock(0, 60, TimeUnit.SECONDS);
            if (!locked) {
                log.debug("任务已被其他实例执行，跳过: {}", funPath);
                return;
            }
            try {
                log.debug("动态任务开始执行: {}", funPath);
                method.invoke(bean);
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
     * 注册定时任务
     */
    public synchronized void addTask(SysTask task) {
        removeTask(task.getFunPath());
        Runnable runnable = resolveTask(task.getFunPath());
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
        Runnable runnable = resolveTask(task.getFunPath());
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
