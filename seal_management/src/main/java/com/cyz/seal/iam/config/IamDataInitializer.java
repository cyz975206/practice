package com.cyz.seal.iam.config;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.cyz.seal.common.context.LegalEntityContext;
import com.cyz.seal.iam.domain.Role;
import com.cyz.seal.iam.domain.RoleScope;
import com.cyz.seal.iam.domain.User;
import com.cyz.seal.iam.domain.UserRole;
import com.cyz.seal.iam.infrastructure.persistence.mapper.RoleMapper;
import com.cyz.seal.iam.infrastructure.persistence.mapper.UserMapper;
import com.cyz.seal.iam.infrastructure.persistence.mapper.UserRoleMapper;
import com.cyz.seal.org.domain.EntityType;
import com.cyz.seal.org.domain.LegalEntity;
import com.cyz.seal.org.infrastructure.persistence.mapper.LegalEntityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 系统种子（幂等、每次启动可重入）：
 * <ol>
 *   <li>默认法人实体（集团本部，全局表）；</li>
 *   <li>三个系统角色（ENTITY 作用域）：super_admin(超级管理员) / admin(系统管理员) / user(普通用户)；</li>
 *   <li>admin 账号（密码取 {@code seal.bootstrap.admin-password}，默认 888888）并授予 super_admin。</li>
 * </ol>
 * GROUP 作用域角色（集团审计/管理员）及其跨实体旁路推迟（ADR-0002）。
 *
 * <p>租户表（角色/用户/用户角色）写入前需 {@link LegalEntityContext#set(Long)} 让多租户拦截器注入
 * legal_entity_id；法人实体是全局表，无需上下文。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamDataInitializer implements ApplicationRunner {

    private static final String DEFAULT_ENTITY_CODE = "GROUP_HQ";
    private static final String ADMIN_USERNAME = "admin";

    /** 系统角色 code（保留 code，管理员自建角色不得占用）。 */
    private static final String SUPER_ADMIN = "super_admin";   // 超级管理员
    private static final String ENTITY_ADMIN = "admin";        // 系统管理员（= 法人实体管理员）
    private static final String NORMAL_USER = "user";          // 普通用户（= 申请人，默认角色）

    private final LegalEntityMapper legalEntityMapper;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${seal.bootstrap.admin-password:888888}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        // 1) 默认法人实体（全局表）：存在则取，无则建
        LegalEntity entity = legalEntityMapper.selectOne(
                Wrappers.<LegalEntity>lambdaQuery().eq(LegalEntity::getCode, DEFAULT_ENTITY_CODE));
        if (entity == null) {
            entity = new LegalEntity();
            entity.setCode(DEFAULT_ENTITY_CODE);
            entity.setFullName("集团本部");
            entity.setShortName("集团本部");
            entity.setEntityType(EntityType.GROUP_HQ);
            entity.setStatus(1);
            legalEntityMapper.insert(entity);
        }
        final Long entityId = entity.getId();

        LegalEntityContext.set(entityId);
        try {
            // 2) 系统角色（幂等：缺哪个建哪个）
            Long superAdminRoleId = ensureRole(SUPER_ADMIN, "超级管理员");
            ensureRole(ENTITY_ADMIN, "系统管理员");
            ensureRole(NORMAL_USER, "普通用户");

            // 3) admin 账号（username 全局唯一）：存在则取，无则建
            User admin = userMapper.selectByUsernameGlobal(ADMIN_USERNAME);
            if (admin == null) {
                admin = new User();
                admin.setUsername(ADMIN_USERNAME);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRealName("超级管理员");
                admin.setStatus(1);
                userMapper.insert(admin);
            }
            // 4) admin 持有 super_admin（幂等）
            ensureUserRole(admin.getId(), superAdminRoleId);
        } finally {
            LegalEntityContext.clear();
        }
        log.info("IAM 种子完成：默认法人实体({}) + 系统角色(super_admin/admin/user) + admin 账号", entityId);
    }

    private Long ensureRole(String code, String name) {
        Role role = roleMapper.selectOne(Wrappers.<Role>lambdaQuery().eq(Role::getCode, code));
        if (role == null) {
            role = new Role();
            role.setCode(code);
            role.setName(name);
            role.setScope(RoleScope.ENTITY);
            role.setStatus(1);
            roleMapper.insert(role);
        }
        return role.getId();
    }

    private void ensureUserRole(Long userId, Long roleId) {
        Long count = userRoleMapper.selectCount(Wrappers.<UserRole>lambdaQuery()
                .eq(UserRole::getUserId, userId).eq(UserRole::getRoleId, roleId));
        if (count == 0) {
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }
}
