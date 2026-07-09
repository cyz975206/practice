package com.cyz.seal.iam.interfaces;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 安全链测试：无 token → 401；带合法 token → 200。
 */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityFilterTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void noTokenReturns401() throws Exception {
        mvc.perform(get("/api/org/legal-entities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void withTokenReturns200() throws Exception {
        String token = loginAdmin();
        mvc.perform(get("/api/org/legal-entities").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private String loginAdmin() throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"888888\"}")).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
    }
}
