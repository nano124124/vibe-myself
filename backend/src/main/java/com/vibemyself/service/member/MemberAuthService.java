package com.vibemyself.service.member;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibemyself.common.jwt.JwtProvider;
import com.vibemyself.common.redis.RedisService;
import com.vibemyself.common.security.LoginUser;
import com.vibemyself.common.util.CookieUtils;
import com.vibemyself.dto.member.LoginMemberRequest;
import com.vibemyself.global.exception.UnauthorizedException;
import com.vibemyself.mapper.member.MemberMapper;
import com.vibemyself.model.member.Member;
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

    private static final String ACTIVE_STATUS = "NORMAL";
    private static final long REFRESH_TTL = 14 * 24 * 3600L;
    private static final long SESSION_TTL = 3600L;

    private final MemberMapper memberMapper;
    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    public void login(LoginMemberRequest request, HttpServletResponse response) {
        Member member = memberMapper.selectByLoginId(request.getLoginId());
        if (member == null || !passwordEncoder.matches(request.getPassword(), member.getLoginPwd())) {
            throw new UnauthorizedException("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
        if (!ACTIVE_STATUS.equals(member.getMbrStatCd())) {
            throw new UnauthorizedException("사용할 수 없는 계정입니다.");
        }

        String id = member.getMbrNo();
        String accessToken = jwtProvider.generateAccessToken(id, "ROLE_USER", "member");
        String refreshToken = jwtProvider.generateRefreshToken(id, "member");

        redisService.save("refresh:member:" + id, refreshToken, REFRESH_TTL);
        saveSession("member", id, toLoginUser(member));
        setCookies(response, accessToken, refreshToken);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = CookieUtils.resolveToken(request, "refresh_token");
        if (refreshToken != null && jwtProvider.isValid(refreshToken)) {
            Claims claims = jwtProvider.parseClaims(refreshToken);
            String id = claims.getSubject();
            redisService.delete("refresh:member:" + id);
            redisService.delete("session:member:" + id);
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
        String stored = redisService.get("refresh:member:" + id);
        if (!refreshToken.equals(stored)) {
            throw new UnauthorizedException("유효하지 않은 refresh token입니다.");
        }

        Member member = memberMapper.selectByMbrNo(id);
        if (member == null || !ACTIVE_STATUS.equals(member.getMbrStatCd())) {
            throw new UnauthorizedException("사용할 수 없는 계정입니다.");
        }

        String newAccessToken = jwtProvider.generateAccessToken(id, "ROLE_USER", "member");
        String newRefreshToken = jwtProvider.generateRefreshToken(id, "member");

        redisService.save("refresh:member:" + id, newRefreshToken, REFRESH_TTL);
        saveSession("member", id, toLoginUser(member));
        setCookies(response, newAccessToken, newRefreshToken);
    }

    public LoginUser loadUser(String id) {
        Member member = memberMapper.selectByMbrNo(id);
        if (member == null || !ACTIVE_STATUS.equals(member.getMbrStatCd())) return null;
        return toLoginUser(member);
    }

    private LoginUser toLoginUser(Member member) {
        return LoginUser.builder()
                .id(member.getMbrNo())
                .loginId(member.getLoginId())
                .name(member.getMbrNm())
                .type("member")
                .role("ROLE_USER")
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
