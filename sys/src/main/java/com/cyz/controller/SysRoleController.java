package com.cyz.controller;

import com.cyz.common.annotation.OperationLog;
import com.cyz.common.enums.OperationType;
import com.cyz.common.result.R;
import com.cyz.dto.request.RoleCreateRequest;
import com.cyz.dto.request.RoleQueryRequest;
import com.cyz.dto.request.RoleUpdateRequest;
import com.cyz.dto.response.RoleResponse;
import com.cyz.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/role")
@RequiredArgsConstructor
public class SysRoleController {

    private final SysRoleService roleService;

    @Operation(summary = "创建角色")
    @OperationLog(module = "角色管理", type = OperationType.ADD, description = "创建角色")
    @PostMapping
    public R<RoleResponse> create(@Valid @RequestBody RoleCreateRequest request) {
        return R.ok(roleService.create(request));
    }

    @Operation(summary = "更新角色")
    @OperationLog(module = "角色管理", type = OperationType.EDIT, description = "更新角色")
    @PutMapping("/{id}")
    public R<RoleResponse> update(@Parameter(description = "角色ID") @PathVariable Long id,
                                   @Valid @RequestBody RoleUpdateRequest request) {
        return R.ok(roleService.update(id, request));
    }

    @Operation(summary = "删除角色")
    @OperationLog(module = "角色管理", type = OperationType.DELETE, description = "删除角色")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "角色ID") @PathVariable Long id) {
        roleService.delete(id);
        return R.ok();
    }

    @Operation(summary = "查询角色详情")
    @GetMapping("/{id}")
    public R<RoleResponse> getById(@Parameter(description = "角色ID") @PathVariable Long id) {
        return R.ok(roleService.getById(id));
    }

    @Operation(summary = "分页查询角色")
    @GetMapping
    public R<Page<RoleResponse>> list(RoleQueryRequest request) {
        return R.ok(roleService.list(request));
    }

    @Operation(summary = "分配菜单")
    @OperationLog(module = "角色管理", type = OperationType.CHANGE_STATUS, description = "分配菜单")
    @PutMapping("/{id}/menus")
    public R<Void> assignMenus(@Parameter(description = "角色ID") @PathVariable Long id,
                                @RequestBody @NotEmpty(message = "菜单ID列表不能为空") List<Long> menuIds) {
        roleService.assignMenus(id, menuIds);
        return R.ok();
    }

    @Operation(summary = "查询角色已分配的菜单ID")
    @GetMapping("/{id}/menus")
    public R<List<Long>> getMenuIds(@Parameter(description = "角色ID") @PathVariable Long id) {
        return R.ok(roleService.getMenuIds(id));
    }
}
