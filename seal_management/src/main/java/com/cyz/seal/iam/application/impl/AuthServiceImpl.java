package com.cyz.seal.iam.application.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyz.seal.common.context.LegalEntityContext;
import com.cyz.seal.common.exception.BusinessException;
import com.cyz.seal.iam.application.AuthService;
import com.cyz.seal.iam.domain.Role;
import com.cyz.seal.iam.domain.User;
import com.cyz.seal.iam.domain.UserRole;
import com.cyz.seal.iam.infrastructure.persistence.mapper.RoleMapper;
import com.cyz.seal.iam.infrastructure.persistence.mapper.UserMapper;
import com.cyz.seal.iam.infrastructure.persistence.mapper.UserRoleMapper;
import com.cyz.seal.iam.infrastructure.security.JwtUtil;
import com.cyz.seal.iam.interfaces.dto.LoginRequest;
import com.cyz.seal.iam.interfaces.dto.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginResponse login(LoginRequest request) {
        // 登录前无租户上下文 → 用忽略租户的全局查询按 username 查（username 全局唯一）
        User user = userMapper.selectByUsernameGlobal(request.username());
        if (user == null || user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        Long legalEntityId = user.getLegalEntityId();
        // 设当前实体上下文后查角色（限定本实体）
        LegalEntityContext.set(legalEntityId);
        List<String> roleCodes = loadRoleCodes(user.getId());
        LegalEntityContext.clear();

        String token = jwtUtil.generate(user.getId(), user.getUsername(), legalEntityId, roleCodes);
        return new LoginResponse(token,
                new LoginResponse.UserInfo(user.getId(), user.getUsername(), user.getNickname(), legalEntityId, roleCodes));
    }

    private List<String> loadRoleCodes(Long userId) {
        List<Long> roleIds = userRoleMapper.selectList(
                        Wrappers.<UserRole>lambdaQuery().eq(UserRole::getUserId, userId)).stream()
                .map(UserRole::getRoleId).toList();
        if (roleIds.isEmpty()) {
            return List.of();
        }
        return roleMapper.selectBatchIds(roleIds).stream().map(Role::getCode).toList();
    }
}
