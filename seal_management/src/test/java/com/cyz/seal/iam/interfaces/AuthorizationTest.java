package com.cyz.seal.iam.interfaces;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 角色级鉴权测试（@PreAuthorize，ADR：粗粒度）：
 * super_admin/admin 可访问管理接口；普通用户(user) → 403；公开接口不受影响。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthorizationTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void adminCanButNormalUserForbidden() throws Exception {
        String adminToken = login("admin", "888888");

        // super_admin 可访问全部管理接口
        mvc.perform(get("/api/iam/users").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/api/org/orgs").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));
        mvc.perform(get("/api/org/legal-entities").header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.code").value(200));

        // 建一个普通用户（自动授 user 角色）并登录
        String username = "norm_" + System.nanoTime();
        mvc.perform(post("/api/iam/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\",\"realName\":\"普通用户\"}"))
                .andExpect(status().isOk());
        String userToken = login(username, "123456");

        // 普通用户访问管理接口 → 403
        mvc.perform(get("/api/iam/users").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/org/orgs").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/api/org/legal-entities").header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
    }
}
