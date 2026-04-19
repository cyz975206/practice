package com.cyz.controller;

import com.cyz.common.result.R;
import com.cyz.dto.request.LoginRequest;
import com.cyz.dto.response.LoginResponse;
import com.cyz.dto.response.LoginUserResponse;
import com.cyz.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "登录")
    @PostMapping("/login")
    public R<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return R.ok(authService.login(request, httpRequest));
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        authService.logout();
        return R.ok();
    }

    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/info")
    public R<LoginUserResponse> getCurrentUser() {
        return R.ok(authService.getCurrentUser());
    }
}
