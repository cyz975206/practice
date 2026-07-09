package com.cyz.seal.iam.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyz.seal.common.result.Result;
import com.cyz.seal.iam.application.RoleService;
import com.cyz.seal.iam.domain.Role;
import com.cyz.seal.iam.interfaces.dto.RoleCreateRequest;
import com.cyz.seal.iam.interfaces.dto.RoleUpdateRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 角色 REST（本法人实体范围）。管理类接口需 系统管理员/超级管理员。
 */
@RestController
@RequestMapping("/api/iam/roles")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('super_admin','admin')")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    public Result<Role> create(@Valid @RequestBody RoleCreateRequest req) {
        return Result.ok(roleService.create(req));
    }

    @GetMapping
    public Result<Page<Role>> page(@RequestParam(defaultValue = "1") long current,
                                   @RequestParam(defaultValue = "10") long size) {
        return Result.ok(roleService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public Result<Role> get(@PathVariable Long id) {
        return Result.ok(roleService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<Role> update(@PathVariable Long id, @Valid @RequestBody RoleUpdateRequest req) {
        return Result.ok(roleService.update(id, req));
    }

    @PutMapping("/{id}/disable")
    public Result<Void> disable(@PathVariable Long id) {
        roleService.disable(id);
        return Result.ok();
    }
}
