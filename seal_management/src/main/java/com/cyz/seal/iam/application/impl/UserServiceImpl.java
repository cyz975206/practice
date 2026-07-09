package com.cyz.seal.iam.application.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyz.seal.common.exception.BusinessException;
import com.cyz.seal.iam.application.UserService;
import com.cyz.seal.iam.domain.User;
import com.cyz.seal.iam.domain.UserRole;
import com.cyz.seal.iam.infrastructure.persistence.mapper.UserMapper;
import com.cyz.seal.iam.infrastructure.persistence.mapper.UserRoleMapper;
import com.cyz.seal.iam.interfaces.dto.UserCreateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User create(UserCreateRequest req) {
        // username 全局唯一：用忽略租户的全局查询校验
        if (baseMapper.selectByUsernameGlobal(req.username()) != null) {
            throw new BusinessException("用户名已存在: " + req.username());
        }
        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setNickname(req.nickname());
        user.setStatus(1);
        save(user); // 拦截器注入当前法人实体的 legal_entity_id
        return user;
    }

    @Override
    @Transactional
    public void assignRoles(Long userId, List<Long> roleIds) {
        userRoleMapper.delete(Wrappers.<UserRole>lambdaQuery().eq(UserRole::getUserId, userId));
        if (roleIds == null) {
            return;
        }
        for (Long roleId : roleIds) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }
}
