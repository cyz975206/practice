package com.cyz.service.impl;

import com.cyz.common.constant.CacheConstant;
import com.cyz.common.exception.BizException;
import com.cyz.converter.TaskConverter;
import com.cyz.dto.request.TaskCreateRequest;
import com.cyz.dto.request.TaskQueryRequest;
import com.cyz.dto.request.TaskUpdateRequest;
import com.cyz.dto.response.TaskResponse;
import com.cyz.entity.SysTask;
import com.cyz.repository.SysTaskRepository;
import com.cyz.service.SysTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SysTaskServiceImpl implements SysTaskService {

    private final SysTaskRepository taskRepository;
    private final TaskConverter taskConverter;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional
    public TaskResponse create(TaskCreateRequest request) {
        if (taskRepository.existsByServiceNameAndFunPathAndIsDeletedFalse(
                request.getServiceName(), request.getFunPath())) {
            throw new BizException("该服务下方法路径已存在: " + request.getFunPath());
        }
        SysTask task = taskConverter.toEntity(request);
        SysTask saved = taskRepository.save(task);
        publishTaskChange(saved.getServiceName());
        return taskConverter.toResponse(saved);
    }

    @Override
    @Transactional
    public TaskResponse update(Long id, TaskUpdateRequest request) {
        SysTask task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("定时任务不存在: " + id));
        taskConverter.updateEntity(task, request);
        SysTask saved = taskRepository.save(task);
        publishTaskChange(saved.getServiceName());
        return taskConverter.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysTask task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("定时任务不存在: " + id));
        task.setIsDeleted(true);
        taskRepository.save(task);
        publishTaskChange(task.getServiceName());
    }

    @Override
    public TaskResponse getById(Long id) {
        SysTask task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("定时任务不存在: " + id));
        return taskConverter.toResponse(task);
    }

    @Override
    public Page<TaskResponse> list(TaskQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<SysTask> page = taskRepository.findByConditions(
                request.getName(), request.getHasStart(), pageRequest);
        return page.map(taskConverter::toResponse);
    }

    @Override
    @Transactional
    public void start(Long id) {
        SysTask task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("定时任务不存在: " + id));
        if (Boolean.TRUE.equals(task.getHasStart())) {
            throw new BizException("任务已处于启用状态");
        }
        task.setHasStart(true);
        taskRepository.save(task);
        publishTaskChange(task.getServiceName());
    }

    @Override
    @Transactional
    public void stop(Long id) {
        SysTask task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("定时任务不存在: " + id));
        if (!Boolean.TRUE.equals(task.getHasStart())) {
            throw new BizException("任务已处于停用状态");
        }
        task.setHasStart(false);
        taskRepository.save(task);
        publishTaskChange(task.getServiceName());
    }

    @Override
    public void trigger(Long id) {
        SysTask task = taskRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("定时任务不存在: " + id));
        String channel = CacheConstant.TASK_CHANNEL_PREFIX + task.getServiceName();
        redisTemplate.convertAndSend(channel, "trigger:" + task.getFunPath());
    }

    private void publishTaskChange(String serviceName) {
        updateTaskCache(serviceName);
        String channel = CacheConstant.TASK_CHANNEL_PREFIX + serviceName;
        redisTemplate.convertAndSend(channel, "refresh");
    }

    private void updateTaskCache(String serviceName) {
        List<SysTask> tasks = taskRepository.findByServiceNameAndIsDeletedFalse(serviceName);
        String listKey = CacheConstant.TASK_LIST_PREFIX + serviceName;
        redisTemplate.delete(listKey);
        if (!tasks.isEmpty()) {
            redisTemplate.opsForList().rightPushAll(listKey, tasks.toArray());
        }
    }
}
