package com.example.controller;

import com.example.dto.CounselorDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 咨询师控制器测试类
 */
@SpringBootTest
@AutoConfigureMockMvc
public class CounselorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    /**
     * 测试searchCounselors接口在没有请求体时能返回全部咨询师列表
     */
    @Test
    public void testSearchCounselorsWithoutBody() throws Exception {
        mockMvc.perform(post("/api/counselors/search")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray());
    }

    /**
     * 测试searchCounselors接口在有请求体时能正确返回筛选后的咨询师列表
     */
    @Test
    public void testSearchCounselorsWithBody() throws Exception {
        String requestBody = "{\"keyword\": \"心理\"}";
        
        mockMvc.perform(post("/api/counselors/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray());
    }
}