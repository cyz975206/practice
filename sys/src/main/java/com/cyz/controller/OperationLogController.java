package com.cyz.controller;

import com.cyz.common.result.R;
import com.cyz.dto.request.OperationLogQueryRequest;
import com.cyz.dto.response.OperationLogResponse;
import com.cyz.service.OperationLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "操作日志")
@RestController
@RequestMapping("/api/log/operation")
@RequiredArgsConstructor
public class OperationLogController {

    private final OperationLogService operationLogService;

    @Operation(summary = "分页查询操作日志")
    @GetMapping
    public R<Page<OperationLogResponse>> list(OperationLogQueryRequest request) {
        return R.ok(operationLogService.list(request));
    }
}
