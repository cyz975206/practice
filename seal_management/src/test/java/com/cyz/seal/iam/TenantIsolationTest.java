package com.cyz.seal.iam;

import com.cyz.seal.common.context.LegalEntityContext;
import com.cyz.seal.iam.domain.User;
import com.cyz.seal.iam.infrastructure.persistence.mapper.UserMapper;
import com.cyz.seal.iam.infrastructure.security.JwtUtil;
import com.cyz.seal.org.domain.EntityType;
import com.cyz.seal.org.domain.LegalEntity;
import com.cyz.seal.org.infrastructure.persistence.mapper.LegalEntityMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 多租户隔离测试：A 实体用户查 sys_user（租户表）应只看到本实体，看不到 B 实体的（连超管也隔离）。
 * 证明 JwtAuthenticationFilter 填充 LegalEntityContext 后，行级隔离真正生效（ADR-0002）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TenantIsolationTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private LegalEntityMapper legalEntityMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;

    @Test
    void userInEntityAOnlySeesOwnEntityUsers() throws Exception {
        LegalEntity a = newEntity("ISO_A", "实体A");
        LegalEntity b = newEntity("ISO_B", "实体B");
        legalEntityMapper.insert(a);
        legalEntityMapper.insert(b);

        Long aUserId = createUserIn(a.getId(), "iso_a_user");
        createUserIn(b.getId(), "iso_b_user");

        // 直接为 A 用户签发 token（legalEntityId = A），绕过登录
        String token = jwtUtil.generate(aUserId, "iso_a_user", a.getId(), List.of("super_admin"));

        mvc.perform(get("/api/iam/users").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.username == 'iso_a_user')]").exists())
                .andExpect(jsonPath("$.data.records[?(@.username == 'iso_b_user')]").doesNotExist());
    }

    private LegalEntity newEntity(String code, String name) {
        LegalEntity e = new LegalEntity();
        e.setCode(code);
        e.setFullName(name);
        e.setShortName(name);
        e.setEntityType(EntityType.GROUP_HQ);
        e.setStatus(1);
        return e;
    }

    private Long createUserIn(Long entityId, String username) {
        LegalEntityContext.set(entityId);
        try {
            User u = new User();
            u.setUsername(username);
            u.setPassword(passwordEncoder.encode("pw"));
            u.setStatus(1);
            userMapper.insert(u); // 拦截器注入 legal_entity_id = entityId
            return u.getId();
        } finally {
            LegalEntityContext.clear();
        }
    }
}
