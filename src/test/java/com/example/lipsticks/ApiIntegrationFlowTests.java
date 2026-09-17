package com.example.lipsticks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationFlowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void loginMallRecommendTryOnFlowShouldWork() throws Exception {
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "username": "user",
                                  "password": "User@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn();

        JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String token = loginJson.path("data").path("token").asText();

        mockMvc.perform(get("/api/mall/products")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(16));

        mockMvc.perform(get("/api/recommend")
                        .param("target", "self")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/visualization/palette"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(16));

        // 试妆接口 —— 由于测试图片是伪数据无法被 ImageIO 解析，
        // 预期返回 400 (IMAGE_READ_FAILED)
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "face.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake-image-content".getBytes()
        );

        String outerLip = "[{\"x\":100,\"y\":200},{\"x\":110,\"y\":190},{\"x\":120,\"y\":185},"
                + "{\"x\":140,\"y\":180},{\"x\":160,\"y\":180},{\"x\":180,\"y\":185},"
                + "{\"x\":190,\"y\":190},{\"x\":200,\"y\":200},{\"x\":190,\"y\":210},"
                + "{\"x\":180,\"y\":215},{\"x\":160,\"y\":218},{\"x\":140,\"y\":218}]";
        String innerLip = "[{\"x\":140,\"y\":195},{\"x\":150,\"y\":192},{\"x\":160,\"y\":190},"
                + "{\"x\":170,\"y\":190},{\"x\":120,\"y\":200},{\"x\":130,\"y\":200},"
                + "{\"x\":160,\"y\":200},{\"x\":170,\"y\":195}]";

        mockMvc.perform(multipart("/api/tryon/upload")
                        .file(file)
                        .param("productId", "1")
                        .param("outerLip", outerLip)
                        .param("innerLip", innerLip)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
