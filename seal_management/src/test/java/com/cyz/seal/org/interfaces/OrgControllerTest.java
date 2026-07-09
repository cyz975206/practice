package com.cyz.seal.org.interfaces;

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
 * 机构 CRUD 集成测试（真实 PG，@Transactional 测后回滚）。
 * 用种子 admin/888888 登录（法人实体=集团本部），机构挂在该实体下。
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrgControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void createTopThenChildRenameDisable() throws Exception {
        String token = loginAdmin();

        // 顶级机构 → ancestors "0,"
        String topCode = "ORG_" + System.nanoTime();
        MvcResult topResult = mvc.perform(post("/api/org/orgs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + topCode + "\",\"name\":\"总部\",\"sort\":1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value(topCode))
                .andExpect(jsonPath("$.data.ancestors").value("0,"))
                .andReturn();
        Long topId = idOf(topResult);

        // 子机构 → ancestors "0,<topId>,"
        String childCode = "ORG_C_" + System.nanoTime();
        MvcResult childResult = mvc.perform(post("/api/org/orgs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + childCode + "\",\"name\":\"研发部\",\"parentId\":" + topId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ancestors").value("0," + topId + ","))
                .andReturn();
        Long childId = idOf(childResult);

        // 列表能查到两个
        mvc.perform(get("/api/org/orgs").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.code == '" + topCode + "')]").exists())
                .andExpect(jsonPath("$.data[?(@.code == '" + childCode + "')]").exists());

        // 改名
        mvc.perform(put("/api/org/orgs/" + childId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"研发中心\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("研发中心"));

        // 停用
        mvc.perform(put("/api/org/orgs/" + childId + "/disable")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void moveSubtreeRecomputesAncestors() throws Exception {
        String token = loginAdmin();

        Long a = createOrg(token, "ORG_A_" + System.nanoTime(), null);
        Long b = createOrg(token, "ORG_B_" + System.nanoTime(), null);
        Long child = createOrg(token, "ORG_AB_" + System.nanoTime(), a); // 挂在 A 下

        // 移到 B 下
        mvc.perform(put("/api/org/orgs/" + child)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"parentId\":" + b + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.ancestors").value("0," + b + ","))
                .andExpect(jsonPath("$.data.parentId").value(b));
    }

    private Long createOrg(String token, String code, Long parentId) throws Exception {
        String body = parentId == null
                ? "{\"code\":\"" + code + "\",\"name\":\"n\"}"
                : "{\"code\":\"" + code + "\",\"name\":\"n\",\"parentId\":" + parentId + "}";
        MvcResult r = mvc.perform(post("/api/org/orgs")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return idOf(r);
    }

    private Long idOf(MvcResult r) throws Exception {
        return ((Number) JsonPath.read(r.getResponse().getContentAsString(), "$.data.id")).longValue();
    }

    private String loginAdmin() throws Exception {
        MvcResult result = mvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"admin\",\"password\":\"888888\"}")).andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.token");
    }
}
