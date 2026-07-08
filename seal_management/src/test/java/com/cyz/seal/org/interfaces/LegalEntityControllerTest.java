package com.cyz.seal.org.interfaces;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 法人实体 CRUD 集成测试（真实 PG，@Transactional 测后回滚，不污染库）。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LegalEntityControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void createThenList() throws Exception {
        String code = "LE_" + System.nanoTime();
        String body = "{\"code\":\"" + code + "\","
                + "\"fullName\":\"测试法人\",\"shortName\":\"测试\",\"entityType\":\"GROUP_HQ\"}";

        // 创建
        mvc.perform(post("/api/org/legal-entities")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.code").value(code))
                .andExpect(jsonPath("$.data.id").exists());

        // 列表能查到（同事务内可见）
        mvc.perform(get("/api/org/legal-entities"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.records[?(@.code == '" + code + "')]").exists());
    }
}
