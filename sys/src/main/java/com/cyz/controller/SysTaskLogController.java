package com.cyz.controller;

import com.cyz.common.result.R;
import com.cyz.dto.request.TaskLogQueryRequest;
import com.cyz.dto.response.TaskLogResponse;
import com.cyz.service.SysTaskLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "任务日志")
@RestController
@RequestMapping("/api/log/task")
@RequiredArgsConstructor
public class SysTaskLogController {

    private final SysTaskLogService sysTaskLogService;

    @Operation(summary = "分页查询任务日志")
    @GetMapping
    public R<Page<TaskLogResponse>> list(TaskLogQueryRequest request) {
        return R.ok(sysTaskLogService.list(request));
    }
}
