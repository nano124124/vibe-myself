package com.vibemyself.controller.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibemyself.common.jwt.JwtProvider;
import com.vibemyself.common.redis.RedisService;
import com.vibemyself.dto.system.LoginAdminRequest;
import com.vibemyself.global.exception.UnauthorizedException;
import com.vibemyself.service.member.MemberAuthService;
import com.vibemyself.service.system.AdminAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminAuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean AdminAuthService adminAuthService;
    @MockBean MemberAuthService memberAuthService;
    @MockBean JwtProvider jwtProvider;
    @MockBean RedisService redisService;

    @Test
    void login_성공_200반환() throws Exception {
        LoginAdminRequest request = new LoginAdminRequest("admin01", "password");

        mockMvc.perform(post("/api/admin/system/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void login_인증실패_401반환() throws Exception {
        willThrow(new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다."))
                .given(adminAuthService).login(any(), any());

        mockMvc.perform(post("/api/admin/system/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginAdminRequest("admin01", "wrong"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_빈값_400반환() throws Exception {
        mockMvc.perform(post("/api/admin/system/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new LoginAdminRequest("", ""))))
                .andExpect(status().isBadRequest());
    }
}
