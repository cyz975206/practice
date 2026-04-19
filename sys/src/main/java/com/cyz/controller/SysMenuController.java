package com.cyz.controller;

import com.cyz.common.annotation.OperationLog;
import com.cyz.common.enums.OperationType;
import com.cyz.common.result.R;
import com.cyz.dto.request.MenuCreateRequest;
import com.cyz.dto.request.MenuQueryRequest;
import com.cyz.dto.request.MenuUpdateRequest;
import com.cyz.dto.response.MenuResponse;
import com.cyz.dto.response.MenuTreeResponse;
import com.cyz.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/menu")
@RequiredArgsConstructor
public class SysMenuController {

    private final SysMenuService menuService;

    @Operation(summary = "创建菜单")
    @OperationLog(module = "菜单管理", type = OperationType.ADD, description = "创建菜单")
    @PostMapping
    public R<MenuResponse> create(@Valid @RequestBody MenuCreateRequest request) {
        return R.ok(menuService.create(request));
    }

    @Operation(summary = "更新菜单")
    @OperationLog(module = "菜单管理", type = OperationType.EDIT, description = "更新菜单")
    @PutMapping("/{id}")
    public R<MenuResponse> update(@Parameter(description = "菜单ID") @PathVariable Long id,
                                   @Valid @RequestBody MenuUpdateRequest request) {
        return R.ok(menuService.update(id, request));
    }

    @Operation(summary = "删除菜单")
    @OperationLog(module = "菜单管理", type = OperationType.DELETE, description = "删除菜单")
    @DeleteMapping("/{id}")
    public R<Void> delete(@Parameter(description = "菜单ID") @PathVariable Long id) {
        menuService.delete(id);
        return R.ok();
    }

    @Operation(summary = "查询菜单详情")
    @GetMapping("/{id}")
    public R<MenuResponse> getById(@Parameter(description = "菜单ID") @PathVariable Long id) {
        return R.ok(menuService.getById(id));
    }

    @Operation(summary = "分页查询菜单")
    @GetMapping
    public R<Page<MenuResponse>> list(MenuQueryRequest request) {
        return R.ok(menuService.list(request));
    }

    @Operation(summary = "获取菜单树")
    @GetMapping("/tree")
    public R<List<MenuTreeResponse>> getTree() {
        return R.ok(menuService.getTree());
    }
}
