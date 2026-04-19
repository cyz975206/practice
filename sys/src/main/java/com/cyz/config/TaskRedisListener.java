package com.cyz.config;

import com.cyz.common.constant.CacheConstant;
import com.cyz.entity.SysTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TaskRedisListener {

    private final RedisConnectionFactory redisConnectionFactory;
    private final DynamicTaskScheduler dynamicTaskScheduler;
    private final TaskProperties taskProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String MSG_REFRESH = "refresh";
    private static final String MSG_TRIGGER_PREFIX = "trigger:";

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer() {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        String channel = CacheConstant.TASK_CHANNEL_PREFIX + taskProperties.getServiceName();
        container.addMessageListener((MessageListener) (message, pattern) -> {
            String body = new String(message.getBody()).replaceAll("\"","");
            log.info("收到Redis任务通知: {}", body);
            if (body.startsWith(MSG_TRIGGER_PREFIX)) {
                String funPath = body.substring(MSG_TRIGGER_PREFIX.length());
                triggerTask(funPath);
            } else {
                refreshTasksFromCache();
            }
        }, new ChannelTopic(channel));
        log.info("已订阅定时任务频道: {}", channel);
        return container;
    }

    @SuppressWarnings("unchecked")
    private void refreshTasksFromCache() {
        String listKey = CacheConstant.TASK_LIST_PREFIX + taskProperties.getServiceName();
        List<Object> cached = redisTemplate.opsForList().range(listKey, 0, -1);
        if (cached == null || cached.isEmpty()) {
            dynamicTaskScheduler.refreshTasks(List.of());
            return;
        }
        List<SysTask> tasks = cached.stream()
                .map(obj -> (SysTask) obj)
                .toList();
        dynamicTaskScheduler.refreshTasks(tasks);
    }

    @SuppressWarnings("unchecked")
    private void triggerTask(String funPath) {
        String listKey = CacheConstant.TASK_LIST_PREFIX + taskProperties.getServiceName();
        List<Object> cached = redisTemplate.opsForList().range(listKey, 0, -1);
        if (cached == null || cached.isEmpty()) {
            return;
        }
        cached.stream()
                .map(obj -> (SysTask) obj)
                .filter(task -> task.getFunPath().equals(funPath))
                .findFirst()
                .ifPresent(dynamicTaskScheduler::executeTaskNow);
    }
}
