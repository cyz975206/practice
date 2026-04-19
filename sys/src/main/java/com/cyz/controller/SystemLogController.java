package com.cyz.controller;

import com.cyz.common.result.R;
import com.cyz.dto.request.SystemLogQueryRequest;
import com.cyz.dto.response.SystemLogResponse;
import com.cyz.service.SystemLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "系统日志")
@RestController
@RequestMapping("/api/log/system")
@RequiredArgsConstructor
public class SystemLogController {

    private final SystemLogService systemLogService;

    @Operation(summary = "分页查询系统日志")
    @GetMapping
    public R<Page<SystemLogResponse>> list(SystemLogQueryRequest request) {
        return R.ok(systemLogService.list(request));
    }
}
