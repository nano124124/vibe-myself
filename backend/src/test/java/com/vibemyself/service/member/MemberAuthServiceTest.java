package com.vibemyself.service.member;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibemyself.common.jwt.JwtProvider;
import com.vibemyself.common.redis.RedisService;
import com.vibemyself.dto.member.LoginMemberRequest;
import com.vibemyself.global.exception.AppException;
import com.vibemyself.entity.EtMbrBase;
import com.vibemyself.mapper.member.MemberMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class MemberAuthServiceTest {

    @Mock MemberMapper memberMapper;
    @Mock RedisService redisService;
    @Mock JwtProvider jwtProvider;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ObjectMapper objectMapper;
    @InjectMocks MemberAuthService memberAuthService;

    private EtMbrBase activeM0001() {
        return EtMbrBase.builder()
                .mbrNo("M0001").loginId("user@test.com").loginPwd("encoded")
                .mbrNm("테스터").grdCd("BASIC").mbrStatCd("NORMAL").build();
    }

    @Test
    void login_회원없음_UnauthorizedException() {
        given(memberMapper.selectByLoginId("unknown@test.com")).willReturn(null);
        assertThatThrownBy(() ->
                memberAuthService.login(new LoginMemberRequest("unknown@test.com", "pw"),
                        new MockHttpServletResponse()))
                .isInstanceOf(AppException.class);
    }

    @Test
    void login_비밀번호틀림_UnauthorizedException() {
        given(memberMapper.selectByLoginId("user@test.com")).willReturn(activeM0001());
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);
        assertThatThrownBy(() ->
                memberAuthService.login(new LoginMemberRequest("user@test.com", "wrong"),
                        new MockHttpServletResponse()))
                .isInstanceOf(AppException.class);
    }

    @Test
    void login_비활성계정_UnauthorizedException() {
        EtMbrBase inactive = EtMbrBase.builder()
                .mbrNo("M0002").loginId("user2@test.com").loginPwd("encoded")
                .mbrNm("비활성").grdCd("BASIC").mbrStatCd("STOP").build();
        given(memberMapper.selectByLoginId("user2@test.com")).willReturn(inactive);
        given(passwordEncoder.matches("pw", "encoded")).willReturn(true);
        assertThatThrownBy(() ->
                memberAuthService.login(new LoginMemberRequest("user2@test.com", "pw"),
                        new MockHttpServletResponse()))
                .isInstanceOf(AppException.class);
    }

    @Test
    void login_성공_Redis에토큰과세션저장() throws Exception {
        given(memberMapper.selectByLoginId("user@test.com")).willReturn(activeM0001());
        given(passwordEncoder.matches("pw", "encoded")).willReturn(true);
        given(jwtProvider.generateAccessToken(any(), any(), any())).willReturn("access");
        given(jwtProvider.generateRefreshToken(any(), any())).willReturn("refresh");
        given(objectMapper.writeValueAsString(any())).willReturn("{}");

        memberAuthService.login(new LoginMemberRequest("user@test.com", "pw"),
                new MockHttpServletResponse());

        then(redisService).should().save(eq("refresh:member:M0001"), eq("refresh"), anyLong());
        then(redisService).should().save(eq("session:member:M0001"), anyString(), anyLong());
    }

    @Test
    void logout_유효한refreshToken_Redis키삭제() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("refresh_token", "refreshToken"));
        given(jwtProvider.isValid("refreshToken")).willReturn(true);
        io.jsonwebtoken.Claims claims = mock(io.jsonwebtoken.Claims.class);
        given(claims.getSubject()).willReturn("M0001");
        given(jwtProvider.parseClaims("refreshToken")).willReturn(claims);

        memberAuthService.logout(request, new MockHttpServletResponse());

        then(redisService).should().delete("refresh:member:M0001");
        then(redisService).should().delete("session:member:M0001");
    }

    @Test
    void logout_refreshToken없음_Redis삭제없이쿠키만초기화() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        memberAuthService.logout(request, response);

        then(redisService).should(never()).delete(any());
        assertThat(response.getHeader("Set-Cookie")).contains("access_token=;");
    }

    @Test
    void refresh_refreshToken없음_UnauthorizedException() {
        assertThatThrownBy(() ->
                memberAuthService.refresh(new MockHttpServletRequest(), new MockHttpServletResponse()))
                .isInstanceOf(AppException.class);
    }

    @Test
    void refresh_Redis저장값불일치_UnauthorizedException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("refresh_token", "refreshToken"));
        given(jwtProvider.isValid("refreshToken")).willReturn(true);
        io.jsonwebtoken.Claims claims = mock(io.jsonwebtoken.Claims.class);
        given(claims.getSubject()).willReturn("M0001");
        given(jwtProvider.parseClaims("refreshToken")).willReturn(claims);
        given(redisService.get("refresh:member:M0001")).willReturn("differentToken");

        assertThatThrownBy(() ->
                memberAuthService.refresh(request, new MockHttpServletResponse()))
                .isInstanceOf(AppException.class);
    }

    @Test
    void loadUser_활성회원_LoginUser반환() {
        given(memberMapper.selectByMbrNo("M0001")).willReturn(activeM0001());
        var loginUser = memberAuthService.loadUser("M0001");
        assertThat(loginUser).isNotNull();
        assertThat(loginUser.getId()).isEqualTo("M0001");
        assertThat(loginUser.getRole()).isEqualTo("ROLE_USER");
        assertThat(loginUser.getGrade()).isEqualTo("BASIC");
    }

    @Test
    void loadUser_비활성회원_null반환() {
        EtMbrBase inactive = EtMbrBase.builder().mbrNo("M0002").mbrStatCd("WDRL").build();
        given(memberMapper.selectByMbrNo("M0002")).willReturn(inactive);
        assertThat(memberAuthService.loadUser("M0002")).isNull();
    }
}
