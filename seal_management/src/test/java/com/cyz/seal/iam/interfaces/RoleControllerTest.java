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

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 角色 CRUD 集成测试（真实 PG，@Transactional 测后回滚）。
 * 含：自定义角色增/查/改/停用；系统保留 code 拒绝创建；系统角色不可停用。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RoleControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void customRoleCrud() throws Exception {
        String token = loginAdmin();
        String code = "ROLE_" + System.nanoTime();

        // 创建自定义角色
        MvcResult r = mvc.perform(post("/api/iam/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"name\":\"自定义角色\",\"scope\":\"ENTITY\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value(code))
                .andReturn();
        Long id = idOf(r);

        // 查
        mvc.perform(get("/api/iam/roles/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("自定义角色"));

        // 改名
        mvc.perform(put("/api/iam/roles/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"改名后\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("改名后"));

        // 停用
        mvc.perform(put("/api/iam/roles/" + id + "/disable").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void reservedCodeCreateRejected() throws Exception {
        String token = loginAdmin();
        mvc.perform(post("/api/iam/roles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"admin\",\"name\":\"占用系统码\",\"scope\":\"ENTITY\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500)); // 业务错误
    }

    @Test
    void systemRoleCannotDisable() throws Exception {
        String token = loginAdmin();
        // 翻页找到 'user' 系统角色 id
        String content = mvc.perform(get("/api/iam/roles").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<?> ids = JsonPath.read(content, "$.data.records[?(@.code == 'user')].id");
        Long userRoleId = ((Number) ids.get(0)).longValue();

        mvc.perform(put("/api/iam/roles/" + userRoleId + "/disable").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(500)); // 系统角色不可停用
    }

    private Long idOf(MvcResult r) throws Exception {
        return ((Number) JsonPath.read(r.getResponse().getContentAsString(), "$.data.id")).longValue();
    }

    private String loginAdmin() throws Exception {
        return login("admin", "888888");
    }

    private String login(String username, String password) throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}")).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
    }
}
