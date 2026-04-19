package com.cyz.controller;

import com.cyz.common.annotation.OperationLog;
import com.cyz.common.enums.OperationType;
import com.cyz.common.result.R;
import com.cyz.dto.request.UserAssignRolesRequest;
import com.cyz.dto.request.UserChangePasswordRequest;
import com.cyz.dto.request.UserCreateRequest;
import com.cyz.dto.request.UserQueryRequest;
import com.cyz.dto.request.UserResetPasswordRequest;
import com.cyz.dto.request.UserUpdateRequest;
import com.cyz.dto.response.UserResponse;
import com.cyz.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class SysUserController {

    private final SysUserService userService;

    @Operation(summary = "创建用户")
    @OperationLog(module = "用户管理", type = OperationType.ADD, description = "创建用户")
    @PostMapping
    public R<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        return R.ok(userService.create(request));
    }

    @Operation(summary = "更新用户")
    @OperationLog(module = "用户管理", type = OperationType.EDIT, description = "更新用户")
    @PutMapping("/{id}")
    public R<UserResponse> update(@Parameter(description = "用户ID") @PathVariable Long id,
                                   @Valid @RequestBody UserUpdateRequest request) {
        return R.ok(userService.update(id, request));
    }

    @Operation(summary = "删除用户")
    @OperationLog(module = "用户管理", type = OperationType.DELETE, description = "删除用户")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.delete(id);
        return R.ok();
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/{id}")
    public R<UserResponse> getById(@Parameter(description = "用户ID") @PathVariable Long id) {
        return R.ok(userService.getById(id));
    }

    @Operation(summary = "分页查询用户")
    @GetMapping
    public R<Page<UserResponse>> list(UserQueryRequest request) {
        return R.ok(userService.list(request));
    }

    @Operation(summary = "重置密码")
    @OperationLog(module = "用户管理", type = OperationType.CHANGE_STATUS, description = "重置密码")
    @PutMapping("/{id}/reset-password")
    public R<String> resetPassword(@Parameter(description = "用户ID") @PathVariable Long id,
                                  @RequestBody(required = false) UserResetPasswordRequest request) {
        if (request == null) {
            request = new UserResetPasswordRequest();
        }
        return R.ok(userService.resetPassword(id, request));
    }

    @Operation(summary = "修改密码")
    @OperationLog(module = "用户管理", type = OperationType.CHANGE_STATUS, description = "修改密码")
    @PutMapping("/change-password")
    public R<Void> changePassword(@Valid @RequestBody UserChangePasswordRequest request) {
        userService.changePassword(request);
        return R.ok();
    }

    @Operation(summary = "解锁用户")
    @OperationLog(module = "用户管理", type = OperationType.CHANGE_STATUS, description = "解锁用户")
    @PutMapping("/{id}/unlock")
    public R<Void> unlock(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.unlock(id);
        return R.ok();
    }

    @Operation(summary = "分配角色")
    @OperationLog(module = "用户管理", type = OperationType.CHANGE_STATUS, description = "分配角色")
    @PutMapping("/{id}/roles")
    public R<Void> assignRoles(@Parameter(description = "用户ID") @PathVariable Long id,
                                @Valid @RequestBody UserAssignRolesRequest request) {
        userService.assignRoles(id, request);
        return R.ok();
    }
}
