package com.cyz.seal.iam.application.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyz.seal.common.exception.BusinessException;
import com.cyz.seal.iam.application.RoleService;
import com.cyz.seal.iam.domain.Role;
import com.cyz.seal.iam.infrastructure.persistence.mapper.RoleMapper;
import com.cyz.seal.iam.interfaces.dto.RoleCreateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {

    @Override
    @Transactional
    public Role create(RoleCreateRequest req) {
        // 编码在本法人实体内唯一（拦截器按当前实体过滤）
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
}
