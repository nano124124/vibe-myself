package com.vibemyself.controller.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibemyself.common.jwt.JwtProvider;
import com.vibemyself.common.redis.RedisService;
import com.vibemyself.dto.member.LoginMemberRequest;
import com.vibemyself.global.exception.AppException;
import com.vibemyself.global.exception.ErrorCode;
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

@WebMvcTest(MemberAuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberAuthControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean MemberAuthService memberAuthService;
    @MockBean AdminAuthService adminAuthService;
    @MockBean JwtProvider jwtProvider;
    @MockBean RedisService redisService;

    @Test
    void login_성공_200반환() throws Exception {
        LoginMemberRequest request = new LoginMemberRequest("user@test.com", "password");

        mockMvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void login_인증실패_401반환() throws Exception {
        LoginMemberRequest request = new LoginMemberRequest("user@test.com", "wrong");
        willThrow(new AppException(ErrorCode.INVALID_CREDENTIALS))
                .given(memberAuthService).login(any(), any());

        mockMvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void login_빈값_400반환() throws Exception {
        LoginMemberRequest request = new LoginMemberRequest("", "");

        mockMvc.perform(post("/api/member/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
