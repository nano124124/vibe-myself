package com.vibemyself.service.system;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibemyself.common.jwt.JwtProvider;
import com.vibemyself.common.redis.RedisService;
import com.vibemyself.common.security.LoginUser;
import com.vibemyself.common.util.CookieUtils;
import com.vibemyself.dto.system.LoginAdminRequest;
import com.vibemyself.global.exception.UnauthorizedException;
import com.vibemyself.mapper.system.AdminMapper;
import com.vibemyself.model.system.Admin;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private static final long REFRESH_TTL = 14 * 24 * 3600L;
    private static final long SESSION_TTL = 3600L;

    private final AdminMapper adminMapper;
    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public void login(LoginAdminRequest request, HttpServletResponse response) {
        Admin admin = adminMapper.selectByLoginId(request.getLoginId());
        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getLoginPwd())) {
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!"Y".equals(admin.getUseYn())) {
            throw new UnauthorizedException("사용할 수 없는 계정입니다.");
        }

        String id = admin.getLoginId();
        String role = toSpringRole(admin.getRoleCd());
        String accessToken = jwtProvider.generateAccessToken(id, role, "admin");
        String refreshToken = jwtProvider.generateRefreshToken(id, "admin");

        redisService.save("refresh:admin:" + id, refreshToken, REFRESH_TTL);
        saveSession("admin", id, toLoginUser(admin));
        setCookies(response, accessToken, refreshToken);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtils.resolveToken(request, "refresh_token");
        if (refreshToken != null && jwtProvider.isValid(refreshToken)) {
            Claims claims = jwtProvider.parseClaims(refreshToken);
            String id = claims.getSubject();
            redisService.delete("refresh:admin:" + id);
            redisService.delete("session:admin:" + id);
        }
        clearCookies(response);
    }

    public void refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtils.resolveToken(request, "refresh_token");
        if (refreshToken == null || !jwtProvider.isValid(refreshToken)) {
            throw new UnauthorizedException("유효하지 않은 refresh token입니다.");
        }

        Claims claims = jwtProvider.parseClaims(refreshToken);
        String id = claims.getSubject();
        String stored = redisService.get("refresh:admin:" + id);
        if (!refreshToken.equals(stored)) {
            throw new UnauthorizedException("유효하지 않은 refresh token입니다.");
        }

        Admin admin = adminMapper.selectByLoginId(id);
        if (admin == null || !"Y".equals(admin.getUseYn())) {
            throw new UnauthorizedException("사용할 수 없는 계정입니다.");
        }

        String role = toSpringRole(admin.getRoleCd());
        String newAccessToken = jwtProvider.generateAccessToken(id, role, "admin");
        String newRefreshToken = jwtProvider.generateRefreshToken(id, "admin");

        redisService.save("refresh:admin:" + id, newRefreshToken, REFRESH_TTL);
        saveSession("admin", id, toLoginUser(admin));
        setCookies(response, newAccessToken, newRefreshToken);
    }

    public LoginUser loadUser(String id) {
        Admin admin = adminMapper.selectByLoginId(id);
        if (admin == null || !"Y".equals(admin.getUseYn())) return null;
        return toLoginUser(admin);
    }

    private LoginUser toLoginUser(Admin admin) {
        return LoginUser.builder()
                .id(admin.getLoginId())
                .loginId(admin.getLoginId())
                .name(admin.getAdminNm())
                .type("admin")
                .role(toSpringRole(admin.getRoleCd()))
                .grade(null)
                .build();
    }

    private String toSpringRole(String roleCd) {
        return "SUPER".equals(roleCd) ? "ROLE_SUPER" : "ROLE_ADMIN";
    }

    private void saveSession(String type, String id, LoginUser loginUser) {
        try {
            redisService.save("session:" + type + ":" + id,
                    objectMapper.writeValueAsString(loginUser), SESSION_TTL);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("세션 저장 실패", e);
        }
    }

    private void setCookies(HttpServletResponse response, String accessToken, String refreshToken) {
        response.addHeader(HttpHeaders.SET_COOKIE,
                CookieUtils.buildCookie("access_token", accessToken, (int) SESSION_TTL).toString());
        response.addHeader(HttpHeaders.SET_COOKIE,
                CookieUtils.buildCookie("refresh_token", refreshToken, (int) REFRESH_TTL).toString());
    }

    private void clearCookies(HttpServletResponse response) {
        response.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.clearCookie("access_token").toString());
        response.addHeader(HttpHeaders.SET_COOKIE, CookieUtils.clearCookie("refresh_token").toString());
    }
}
