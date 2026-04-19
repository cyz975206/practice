package com.cyz.controller;

import com.cyz.common.result.R;
import com.cyz.dto.request.SecurityLogQueryRequest;
import com.cyz.dto.response.SecurityLogResponse;
import com.cyz.service.SecurityLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "安全日志")
@RestController
@RequestMapping("/api/log/security")
@RequiredArgsConstructor
public class SecurityLogController {

    private final SecurityLogService securityLogService;

    @Operation(summary = "分页查询安全日志")
    @GetMapping
    public R<Page<SecurityLogResponse>> list(SecurityLogQueryRequest request) {
        return R.ok(securityLogService.list(request));
    }

    @Operation(summary = "处理安全日志")
    @PutMapping("/{id}/handle")
    public R<Void> handle(@Parameter(description = "安全日志ID") @PathVariable Long id,
                           @Parameter(description = "处理状态（handled/ignored）") @RequestParam String handleStatus,
                           @Parameter(description = "处理备注") @RequestParam(required = false) String handleNote) {
        securityLogService.handle(id, handleStatus, handleNote);
        return R.ok();
    }
}
