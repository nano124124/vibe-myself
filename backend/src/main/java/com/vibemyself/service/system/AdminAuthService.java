package com.vibemyself.service.system;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibemyself.common.jwt.JwtProvider;
import com.vibemyself.common.redis.RedisService;
import com.vibemyself.common.security.LoginUser;
import com.vibemyself.common.util.CookieUtils;
import com.vibemyself.dto.system.LoginAdminRequest;
import com.vibemyself.enums.RoleCode;
import java.util.Objects;
import com.vibemyself.enums.UserType;
import com.vibemyself.global.exception.AppException;
import com.vibemyself.global.exception.ErrorCode;
import com.vibemyself.entity.StAdminBase;
import com.vibemyself.mapper.system.AdminMapper;
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
        StAdminBase admin = adminMapper.selectByLoginId(request.getLoginId());
        if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getLoginPwd())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!"Y".equals(admin.getUseYn())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        String id = admin.getLoginId();
        String role = RoleCode.from(admin.getRoleCd()).toSpringRole();
        String accessToken = jwtProvider.generateAccessToken(id, role, UserType.ADMIN.getValue());
        String refreshToken = jwtProvider.generateRefreshToken(id, UserType.ADMIN.getValue());

        redisService.save("refresh:" + UserType.ADMIN.getValue() + ":" + id, refreshToken, REFRESH_TTL);
        saveSession(UserType.ADMIN.getValue(), id, toLoginUser(admin));
        setCookies(response, accessToken, refreshToken);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtils.resolveToken(request, "refresh_token");
        if (refreshToken != null && jwtProvider.isValid(refreshToken)) {
            Claims claims = jwtProvider.parseClaims(refreshToken);
            String id = claims.getSubject();
            redisService.delete("refresh:" + UserType.ADMIN.getValue() + ":" + id);
            redisService.delete("session:" + UserType.ADMIN.getValue() + ":" + id);
        }
        clearCookies(response);
    }

    public void refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtils.resolveToken(request, "refresh_token");
        if (refreshToken == null || !jwtProvider.isValid(refreshToken)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        Claims claims = jwtProvider.parseClaims(refreshToken);
        String id = claims.getSubject();
        String stored = redisService.get("refresh:" + UserType.ADMIN.getValue() + ":" + id);
        if (!Objects.equals(refreshToken, stored)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        StAdminBase admin = adminMapper.selectByLoginId(id);
        if (admin == null || !"Y".equals(admin.getUseYn())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        String role = RoleCode.from(admin.getRoleCd()).toSpringRole();
        String newAccessToken = jwtProvider.generateAccessToken(id, role, UserType.ADMIN.getValue());
        String newRefreshToken = jwtProvider.generateRefreshToken(id, UserType.ADMIN.getValue());

        redisService.save("refresh:" + UserType.ADMIN.getValue() + ":" + id, newRefreshToken, REFRESH_TTL);
        saveSession(UserType.ADMIN.getValue(), id, toLoginUser(admin));
        setCookies(response, newAccessToken, newRefreshToken);
    }

    public LoginUser loadUser(String id) {
        StAdminBase admin = adminMapper.selectByLoginId(id);
        if (admin == null || !"Y".equals(admin.getUseYn())) return null;
        return toLoginUser(admin);
    }

    private LoginUser toLoginUser(StAdminBase admin) {
        return LoginUser.builder()
                .id(admin.getLoginId())
                .loginId(admin.getLoginId())
                .name(admin.getAdminNm())
                .type(UserType.ADMIN.getValue())
                .role(RoleCode.from(admin.getRoleCd()).toSpringRole())
                .grade(null)
                .build();
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
