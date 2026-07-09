package com.cyz.seal.iam.config;

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
 * 首次初始化种子：系统空时（无用户）建默认法人实体（集团本部）+ 超级管理员角色 + admin 用户（密码取
 * {@code seal.bootstrap.admin-password}，默认 888888）+ 分配角色。幂等。
 *
 * <p>角色/用户/用户角色是租户表，插入前需 {@link LegalEntityContext#set(Long)} 让多租户拦截器注入
 * legal_entity_id；法人实体是全局表，无需上下文。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IamDataInitializer implements ApplicationRunner {

    private static final String DEFAULT_ENTITY_CODE = "GROUP_HQ";
    private static final String SUPER_ADMIN_ROLE = "super_admin";

    private final LegalEntityMapper legalEntityMapper;
    private final RoleMapper roleMapper;
    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${seal.bootstrap.admin-password:888888}")
    private String adminPassword;

    @Override
    public void run(ApplicationArguments args) {
        // 幂等判定：查全局表 legal_entity 的计数（拦截器忽略全局表，计数准确）。
        // 不查 sys_user：无租户上下文时 sys_user 会被加 legal_entity_id=null 条件、计为 0，导致误判为"未初始化"。
        if (legalEntityMapper.selectCount(null) > 0) {
            return;
        }

        // 1) 默认法人实体（全局表）
        LegalEntity entity = new LegalEntity();
        entity.setCode(DEFAULT_ENTITY_CODE);
        entity.setFullName("集团本部");
        entity.setShortName("集团本部");
        entity.setEntityType(EntityType.GROUP_HQ);
        entity.setStatus(1);
        legalEntityMapper.insert(entity);
        Long entityId = entity.getId();

        // 2-4) 租户表：设上下文让拦截器注入 legal_entity_id
        LegalEntityContext.set(entityId);
        try {
            Role role = new Role();
            role.setCode(SUPER_ADMIN_ROLE);
            role.setName("超级管理员");
            role.setScope(RoleScope.ENTITY);
            role.setStatus(1);
            roleMapper.insert(role);

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRealName("超级管理员");
            admin.setStatus(1);
            userMapper.insert(admin);

            UserRole userRole = new UserRole();
            userRole.setUserId(admin.getId());
            userRole.setRoleId(role.getId());
            userRoleMapper.insert(userRole);
        } finally {
            LegalEntityContext.clear();
        }

        log.info("IAM 首次初始化完成：默认法人实体({}) + 超级管理员角色 + admin 用户（密码见 seal.bootstrap.admin-password）", entityId);
    }
}
