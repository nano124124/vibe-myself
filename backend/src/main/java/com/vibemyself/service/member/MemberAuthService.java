package com.vibemyself.service.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibemyself.common.jwt.JwtProvider;
import com.vibemyself.common.redis.RedisService;
import com.vibemyself.common.security.LoginUser;
import com.vibemyself.common.util.CookieUtils;
import com.vibemyself.dto.member.LoginMemberRequest;
import com.vibemyself.enums.MemberStatus;
import java.util.Objects;
import com.vibemyself.enums.RoleCode;
import com.vibemyself.enums.UserType;
import com.vibemyself.global.exception.AppException;
import com.vibemyself.global.exception.ErrorCode;
import com.vibemyself.entity.EtMbrBase;
import com.vibemyself.mapper.member.MemberMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberAuthService {

    private static final long REFRESH_TTL = 14 * 24 * 3600L;
    private static final long SESSION_TTL = 3600L;

    private final MemberMapper memberMapper;
    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public void login(LoginMemberRequest request, HttpServletResponse response) {
        EtMbrBase member = memberMapper.selectByLoginId(request.getLoginId());
        if (member == null || !passwordEncoder.matches(request.getPassword(), member.getLoginPwd())) {
            throw new AppException(ErrorCode.INVALID_CREDENTIALS);
        }
        if (!MemberStatus.NORMAL.getCode().equals(member.getMbrStatCd())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        String id = member.getMbrNo();
        String accessToken = jwtProvider.generateAccessToken(id, RoleCode.USER.toSpringRole(), UserType.MEMBER.getValue());
        String refreshToken = jwtProvider.generateRefreshToken(id, UserType.MEMBER.getValue());

        redisService.save("refresh:member:" + id, refreshToken, REFRESH_TTL);
        saveSession(UserType.MEMBER.getValue(), id, toLoginUser(member));
        setCookies(response, accessToken, refreshToken);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtils.resolveToken(request, "refresh_token");
        if (refreshToken != null && jwtProvider.isValid(refreshToken)) {
            Claims claims = jwtProvider.parseClaims(refreshToken);
            String id = claims.getSubject();
            redisService.delete("refresh:" + UserType.MEMBER.getValue() + ":" + id);
            redisService.delete("session:" + UserType.MEMBER.getValue() + ":" + id);
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
        String stored = redisService.get("refresh:" + UserType.MEMBER.getValue() + ":" + id);
        if (!Objects.equals(refreshToken, stored)) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        EtMbrBase member = memberMapper.selectByMbrNo(id);
        if (member == null || !MemberStatus.NORMAL.getCode().equals(member.getMbrStatCd())) {
            throw new AppException(ErrorCode.ACCOUNT_DISABLED);
        }

        String newAccessToken = jwtProvider.generateAccessToken(id, RoleCode.USER.toSpringRole(), UserType.MEMBER.getValue());
        String newRefreshToken = jwtProvider.generateRefreshToken(id, UserType.MEMBER.getValue());

        redisService.save("refresh:" + UserType.MEMBER.getValue() + ":" + id, newRefreshToken, REFRESH_TTL);
        saveSession(UserType.MEMBER.getValue(), id, toLoginUser(member));
        setCookies(response, newAccessToken, newRefreshToken);
    }

    public LoginUser loadUser(String id) {
        EtMbrBase member = memberMapper.selectByMbrNo(id);
        if (member == null || !MemberStatus.NORMAL.getCode().equals(member.getMbrStatCd())) return null;
        return toLoginUser(member);
    }

    private LoginUser toLoginUser(EtMbrBase member) {
        return LoginUser.builder()
                .id(member.getMbrNo())
                .loginId(member.getLoginId())
                .name(member.getMbrNm())
                .type(UserType.MEMBER.getValue())
                .role(RoleCode.USER.toSpringRole())
                .grade(member.getGrdCd())
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
