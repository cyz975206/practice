package com.cyz.seal.iam.application.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyz.seal.common.exception.BusinessException;
import com.cyz.seal.iam.application.UserService;
import com.cyz.seal.iam.domain.Role;
import com.cyz.seal.iam.domain.User;
import com.cyz.seal.iam.domain.UserRole;
import com.cyz.seal.iam.infrastructure.persistence.mapper.RoleMapper;
import com.cyz.seal.iam.infrastructure.persistence.mapper.UserMapper;
import com.cyz.seal.iam.infrastructure.persistence.mapper.UserRoleMapper;
import com.cyz.seal.iam.interfaces.dto.UserCreateRequest;
import com.cyz.seal.iam.interfaces.dto.UserUpdateRequest;
import com.cyz.seal.org.infrastructure.persistence.mapper.OrgMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /** 普通用户角色 code（新建用户默认授予，CONTEXT：申请人=所有用户的默认角色）。 */
    private static final String DEFAULT_ROLE_CODE = "user";

    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final OrgMapper orgMapper;   // 校验 org_id 同法人实体
    private final RoleMapper roleMapper; // 默认角色授予

    @Override
    @Transactional
    public User create(UserCreateRequest req) {
        // username 全局唯一：用忽略租户的全局查询校验
        if (baseMapper.selectByUsernameGlobal(req.username()) != null) {
            throw new BusinessException("用户名已存在: " + req.username());
        }
        validateOrgInCurrentEntity(req.orgId());
        String employeeNo = normalize(req.employeeNo());
        validateEmployeeNoUnique(employeeNo, null);

        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setRealName(req.realName());
        user.setEmployeeNo(employeeNo);
        user.setPhone(req.phone());
        user.setOrgId(req.orgId());
        user.setPosition(req.position());
        user.setStatus(1);
        save(user); // 拦截器注入当前法人实体的 legal_entity_id
        assignDefaultRole(user.getId());
        return user;
    }

    @Override
    @Transactional
    public User update(Long userId, UserUpdateRequest req) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在: " + userId);
        }
        if (req.realName() != null) {
            user.setRealName(req.realName());
        }
        if (req.phone() != null) {
            user.setPhone(req.phone());
        }
        if (req.position() != null) {
            user.setPosition(req.position());
        }
        if (req.status() != null) {
            user.setStatus(req.status());
        }
        if (req.orgId() != null) {
            validateOrgInCurrentEntity(req.orgId());
            user.setOrgId(req.orgId());
        }
        if (req.employeeNo() != null) {
            String employeeNo = normalize(req.employeeNo());
            validateEmployeeNoUnique(employeeNo, userId);
            user.setEmployeeNo(employeeNo);
        }
        updateById(user);
        return user;
    }

    @Override
    @Transactional
    public void resetPassword(Long userId, String newPassword) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在: " + userId);
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        updateById(user);
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

    /** 新建用户默认授予"普通用户"角色（当前法人实体内按 code 查；未配置则跳过）。 */
    private void assignDefaultRole(Long userId) {
        Role userRole = roleMapper.selectOne(Wrappers.<Role>lambdaQuery().eq(Role::getCode, DEFAULT_ROLE_CODE));
        if (userRole != null) {
            UserRole ur = new UserRole();
            ur.setUserId(userId);
            ur.setRoleId(userRole.getId());
            userRoleMapper.insert(ur);
        }
    }

    /** 工号在本法人实体内唯一（拦截器按当前实体过滤；排除自身）。空/空白视为不设置。 */
    private void validateEmployeeNoUnique(String employeeNo, Long excludeUserId) {
        if (employeeNo == null) {
            return;
        }
        var q = lambdaQuery().eq(User::getEmployeeNo, employeeNo);
        if (excludeUserId != null) {
            q.ne(User::getId, excludeUserId);
        }
        if (q.exists()) {
            throw new BusinessException("工号已存在: " + employeeNo);
        }
    }

    /** org_id 必须属于当前法人实体（sys_org 是租户表，拦截器自动过滤他实体）。 */
    private void validateOrgInCurrentEntity(Long orgId) {
        if (orgId == null) {
            return;
        }
        if (orgMapper.selectById(orgId) == null) {
            throw new BusinessException("所属部门不存在或不属于当前法人实体: " + orgId);
        }
    }

    /** 空白字符串归一为 null（避免工号空串触发唯一索引冲突）。 */
    private String normalize(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
