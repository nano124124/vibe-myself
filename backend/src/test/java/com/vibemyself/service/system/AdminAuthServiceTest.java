package com.vibemyself.service.system;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibemyself.common.jwt.JwtProvider;
import com.vibemyself.common.redis.RedisService;
import com.vibemyself.common.security.LoginUser;
import com.vibemyself.dto.system.LoginAdminRequest;
import com.vibemyself.global.exception.UnauthorizedException;
import com.vibemyself.entity.StAdminBase;
import com.vibemyself.mapper.system.AdminMapper;
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
class AdminAuthServiceTest {

    @Mock AdminMapper adminMapper;
    @Mock RedisService redisService;
    @Mock JwtProvider jwtProvider;
    @Mock PasswordEncoder passwordEncoder;
    @Mock ObjectMapper objectMapper;
    @InjectMocks AdminAuthService adminAuthService;

    private StAdminBase activeAdmin(String roleCd) {
        return StAdminBase.builder()
                .loginId("admin01").loginPwd("encoded")
                .adminNm("관리자1").roleCd(roleCd).useYn("Y").build();
    }

    @Test
    void login_관리자없음_UnauthorizedException() {
        given(adminMapper.selectByLoginId("unknown")).willReturn(null);
        assertThatThrownBy(() ->
                adminAuthService.login(new LoginAdminRequest("unknown", "pw"),
                        new MockHttpServletResponse()))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_비활성계정_UnauthorizedException() {
        StAdminBase inactive = StAdminBase.builder().loginId("admin02").loginPwd("encoded")
                .adminNm("비활성").roleCd("ADMIN").useYn("N").build();
        given(adminMapper.selectByLoginId("admin02")).willReturn(inactive);
        given(passwordEncoder.matches("pw", "encoded")).willReturn(true);
        assertThatThrownBy(() ->
                adminAuthService.login(new LoginAdminRequest("admin02", "pw"),
                        new MockHttpServletResponse()))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void login_성공_SUPER역할_ROLE_SUPER반환() throws Exception {
        given(adminMapper.selectByLoginId("admin01")).willReturn(activeAdmin("SUPER"));
        given(passwordEncoder.matches("pw", "encoded")).willReturn(true);
        given(jwtProvider.generateAccessToken(any(), any(), any())).willReturn("access");
        given(jwtProvider.generateRefreshToken(any(), any())).willReturn("refresh");
        given(objectMapper.writeValueAsString(any())).willReturn("{}");

        adminAuthService.login(new LoginAdminRequest("admin01", "pw"),
                new MockHttpServletResponse());

        then(jwtProvider).should().generateAccessToken("admin01", "ROLE_SUPER", "admin");
    }

    @Test
    void loadUser_활성관리자_LoginUser반환() {
        given(adminMapper.selectByLoginId("admin01")).willReturn(activeAdmin("ADMIN"));
        LoginUser loginUser = adminAuthService.loadUser("admin01");
        assertThat(loginUser).isNotNull();
        assertThat(loginUser.getId()).isEqualTo("admin01");
        assertThat(loginUser.getRole()).isEqualTo("ROLE_ADMIN");
        assertThat(loginUser.getGrade()).isNull();
    }

    @Test
    void loadUser_비활성관리자_null반환() {
        StAdminBase inactive = StAdminBase.builder().loginId("admin02").loginPwd("encoded")
                .adminNm("비활성").roleCd("ADMIN").useYn("N").build();
        given(adminMapper.selectByLoginId("admin02")).willReturn(inactive);
        assertThat(adminAuthService.loadUser("admin02")).isNull();
    }

    @Test
    void logout_유효한refreshToken_Redis키삭제() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("refresh_token", "refreshToken"));
        given(jwtProvider.isValid("refreshToken")).willReturn(true);
        io.jsonwebtoken.Claims claims = mock(io.jsonwebtoken.Claims.class);
        given(claims.getSubject()).willReturn("admin01");
        given(jwtProvider.parseClaims("refreshToken")).willReturn(claims);

        adminAuthService.logout(request, new MockHttpServletResponse());

        then(redisService).should().delete("refresh:admin:admin01");
        then(redisService).should().delete("session:admin:admin01");
    }

    @Test
    void refresh_Redis저장값불일치_UnauthorizedException() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new jakarta.servlet.http.Cookie("refresh_token", "refreshToken"));
        given(jwtProvider.isValid("refreshToken")).willReturn(true);
        io.jsonwebtoken.Claims claims = mock(io.jsonwebtoken.Claims.class);
        given(claims.getSubject()).willReturn("admin01");
        given(jwtProvider.parseClaims("refreshToken")).willReturn(claims);
        given(redisService.get("refresh:admin:admin01")).willReturn("differentToken");

        assertThatThrownBy(() ->
                adminAuthService.refresh(request, new MockHttpServletResponse()))
                .isInstanceOf(UnauthorizedException.class);
    }
}
