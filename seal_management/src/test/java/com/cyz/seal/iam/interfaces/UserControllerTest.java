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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 用户（人员）CRUD 集成测试（真实 PG，@Transactional 测后回滚）。
 * 用种子 admin/888888 登录；新建用户挂在 admin 的法人实体下。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void createUpdateResetPassword() throws Exception {
        String token = loginAdmin();
        String username = "u_" + System.nanoTime();

        MvcResult r = mvc.perform(post("/api/iam/users")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"123456\",\"realName\":\"张三\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.realName").value("张三"))
                .andReturn();
        Long id = ((Number) JsonPath.read(r.getResponse().getContentAsString(), "$.data.id")).longValue();

        // 更新人员信息
        mvc.perform(put("/api/iam/users/" + id)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"realName\":\"张三丰\",\"phone\":\"13800000000\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.realName").value("张三丰"))
                .andExpect(jsonPath("$.data.phone").value("13800000000"));

        // 重置密码
        mvc.perform(put("/api/iam/users/" + id + "/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"password\":\"newpw\"}"))
                .andExpect(status().isOk());

        // 查回
        mvc.perform(get("/api/iam/users/" + id).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.username").value(username));
    }

    private String loginAdmin() throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"888888\"}")).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
    }
}
