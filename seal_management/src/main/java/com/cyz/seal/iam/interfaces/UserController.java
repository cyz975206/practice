package com.cyz.seal.iam.interfaces;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cyz.seal.common.result.Result;
import com.cyz.seal.iam.application.UserService;
import com.cyz.seal.iam.domain.User;
import com.cyz.seal.iam.interfaces.dto.AssignRolesRequest;
import com.cyz.seal.iam.interfaces.dto.UserCreateRequest;
import com.cyz.seal.iam.interfaces.dto.UserUpdateRequest;
import com.cyz.seal.iam.interfaces.dto.ResetPasswordRequest;
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

/** 用户 REST（本法人实体范围）。 */
@RestController
@RequestMapping("/api/iam/users")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('super_admin','admin')")
public class UserController {

    private final UserService userService;

    @PostMapping
    public Result<User> create(@Valid @RequestBody UserCreateRequest req) {
        return Result.ok(userService.create(req));
    }

    @GetMapping
    public Result<Page<User>> page(@RequestParam(defaultValue = "1") long current,
                                   @RequestParam(defaultValue = "10") long size) {
        return Result.ok(userService.page(new Page<>(current, size)));
    }

    @GetMapping("/{id}")
    public Result<User> get(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest req) {
        return Result.ok(userService.update(id, req));
    }

    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest req) {
        userService.resetPassword(id, req.password());
        return Result.ok();
    }

    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(@PathVariable Long id, @Valid @RequestBody AssignRolesRequest req) {
        userService.assignRoles(id, req.roleIds());
        return Result.ok();
    }
}
