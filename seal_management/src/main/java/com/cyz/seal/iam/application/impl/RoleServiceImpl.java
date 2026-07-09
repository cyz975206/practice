package com.cyz.seal.iam.application.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyz.seal.common.exception.BusinessException;
import com.cyz.seal.iam.application.RoleService;
import com.cyz.seal.iam.domain.Role;
import com.cyz.seal.iam.infrastructure.persistence.mapper.RoleMapper;
import com.cyz.seal.iam.interfaces.dto.RoleCreateRequest;
import com.cyz.seal.iam.interfaces.dto.RoleUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    /** 系统保留角色 code：不可停用（避免把 admin/user/super_admin 关掉致系统不可用）。 */
    private static final java.util.Set<String> SYSTEM_ROLE_CODES =
            java.util.Set.of("super_admin", "admin", "user");

    @Override
    @Transactional
    public Role create(RoleCreateRequest req) {
        // 编码在本法人实体内唯一（拦截器按当前实体过滤）；且不得占用系统保留 code
        if (SYSTEM_ROLE_CODES.contains(req.code())) {
            throw new BusinessException("角色编码为系统保留，不可使用: " + req.code());
        }
        if (lambdaQuery().eq(Role::getCode, req.code()).exists()) {
            throw new BusinessException("角色编码已存在: " + req.code());
        }
        Role role = new Role();
        role.setCode(req.code());
        role.setName(req.name());
        role.setScope(req.scope());
        role.setStatus(1);
        save(role);
        return role;
    }

    @Override
    @Transactional
    public Role update(Long id, RoleUpdateRequest req) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在: " + id);
        }
        // code 与 scope 锁定不可改：仅 name/status
        if (req.name() != null) {
            role.setName(req.name());
        }
        if (req.status() != null) {
            role.setStatus(req.status());
        }
        updateById(role);
        return role;
    }

    @Override
    @Transactional
    public void disable(Long id) {
        Role role = getById(id);
        if (role == null) {
            throw new BusinessException("角色不存在: " + id);
        }
        if (SYSTEM_ROLE_CODES.contains(role.getCode())) {
            throw new BusinessException("系统角色不可停用: " + role.getCode());
        }
        role.setStatus(0);
        updateById(role);
    }
}
