package com.cyz.controller;

import com.cyz.common.annotation.OperationLog;
import com.cyz.common.enums.OperationType;
import com.cyz.common.result.R;
import com.cyz.dto.request.TaskCreateRequest;
import com.cyz.dto.request.TaskQueryRequest;
import com.cyz.dto.request.TaskUpdateRequest;
import com.cyz.dto.response.TaskResponse;
import com.cyz.service.SysTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "定时任务管理")
@RestController
@RequestMapping("/api/task")
@RequiredArgsConstructor
public class SysTaskController {

    private final SysTaskService taskService;

    @Operation(summary = "创建定时任务")
    @OperationLog(module = "定时任务", type = OperationType.ADD, description = "创建定时任务")
    @PostMapping
    public R<TaskResponse> create(@Valid @RequestBody TaskCreateRequest request) {
        return R.ok(taskService.create(request));
    }

    @Operation(summary = "更新定时任务")
    @OperationLog(module = "定时任务", type = OperationType.EDIT, description = "更新定时任务")
    @PutMapping("/{id}")
    public R<TaskResponse> update(@Parameter(description = "任务ID") @PathVariable Long id,
                                   @Valid @RequestBody TaskUpdateRequest request) {
        return R.ok(taskService.update(id, request));
    }

    @Operation(summary = "删除定时任务")
    @OperationLog(module = "定时任务", type = OperationType.DELETE, description = "删除定时任务")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "任务ID") @PathVariable Long id) {
        taskService.delete(id);
        return R.ok();
    }

    @Operation(summary = "查询定时任务详情")
    @GetMapping("/{id}")
    public R<TaskResponse> getById(@Parameter(description = "任务ID") @PathVariable Long id) {
        return R.ok(taskService.getById(id));
    }

    @Operation(summary = "分页查询定时任务")
    @GetMapping
    public R<Page<TaskResponse>> list(TaskQueryRequest request) {
        return R.ok(taskService.list(request));
    }

    @Operation(summary = "启动定时任务")
    @OperationLog(module = "定时任务", type = OperationType.CHANGE_STATUS, description = "启动定时任务")
    @PutMapping("/{id}/start")
    public R<Void> start(@Parameter(description = "任务ID") @PathVariable Long id) {
        taskService.start(id);
        return R.ok();
    }

    @Operation(summary = "停止定时任务")
    @OperationLog(module = "定时任务", type = OperationType.CHANGE_STATUS, description = "停止定时任务")
    @PutMapping("/{id}/stop")
    public R<Void> stop(@Parameter(description = "任务ID") @PathVariable Long id) {
        taskService.stop(id);
        return R.ok();
    }

    @Operation(summary = "手动触发定时任务")
    @OperationLog(module = "定时任务", type = OperationType.EDIT, description = "手动触发定时任务")
    @PostMapping("/{id}/trigger")
    public R<Void> trigger(@Parameter(description = "任务ID") @PathVariable Long id) {
        taskService.trigger(id);
        return R.ok();
    }
}
