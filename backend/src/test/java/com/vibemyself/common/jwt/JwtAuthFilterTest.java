package com.vibemyself.common.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibemyself.common.redis.RedisService;
import com.vibemyself.common.security.LoginUser;
import com.vibemyself.service.member.MemberAuthService;
import com.vibemyself.service.system.AdminAuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock JwtProvider jwtProvider;
    @Mock RedisService redisService;
    @Mock MemberAuthService memberAuthService;
    @Mock AdminAuthService adminAuthService;
    @Mock FilterChain filterChain;

    JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(jwtProvider, redisService, memberAuthService, adminAuthService, new ObjectMapper());
        SecurityContextHolder.clearContext();
    }

    @Test
    void 토큰없음_필터통과_SecurityContext비어있음() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilterInternal(request, response, filterChain);

        then(filterChain).should().doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void 유효한토큰_Redis세션있음_SecurityContext세팅() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("access_token", "validToken"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtProvider.isValid("validToken")).willReturn(true);
        Claims mockClaims = mock(Claims.class);
        given(mockClaims.getSubject()).willReturn("M0001");
        given(mockClaims.get("type", String.class)).willReturn("member");
        given(jwtProvider.parseClaims("validToken")).willReturn(mockClaims);

        LoginUser loginUser = LoginUser.builder()
                .id("M0001").loginId("user@test.com").name("테스터")
                .type("member").role("ROLE_USER").grade("BASIC").build();
        given(redisService.get("session:member:M0001"))
                .willReturn(new ObjectMapper().writeValueAsString(loginUser));

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .isInstanceOf(LoginUser.class);
    }

    @Test
    void 유효하지않은토큰_401반환() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("access_token", "invalidToken"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtProvider.isValid("invalidToken")).willReturn(false);
        given(jwtProvider.isExpired("invalidToken")).willReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        then(filterChain).should(never()).doFilter(any(), any());
    }

    @Test
    void 만료된토큰_SecurityContext없이_필터통과() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("access_token", "expiredToken"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        given(jwtProvider.isValid("expiredToken")).willReturn(false);
        given(jwtProvider.isExpired("expiredToken")).willReturn(true);

        Claims mockClaims = mock(Claims.class);
        given(mockClaims.getSubject()).willReturn("M0001");
        given(mockClaims.get("type", String.class)).willReturn("member");
        given(jwtProvider.parseClaimsIgnoreExpiry("expiredToken")).willReturn(mockClaims);

        filter.doFilterInternal(request, response, filterChain);

        then(filterChain).should().doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
