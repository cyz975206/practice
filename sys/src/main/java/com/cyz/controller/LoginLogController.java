package com.cyz.controller;

import com.cyz.common.result.R;
import com.cyz.dto.request.LoginLogQueryRequest;
import com.cyz.dto.response.LoginLogResponse;
import com.cyz.service.LoginLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "登录日志")
@RestController
@RequestMapping("/api/log/login")
@RequiredArgsConstructor
public class LoginLogController {

    private final LoginLogService loginLogService;

    @Operation(summary = "分页查询登录日志")
    @GetMapping
    public R<Page<LoginLogResponse>> list(LoginLogQueryRequest request) {
        return R.ok(loginLogService.list(request));
    }
}
