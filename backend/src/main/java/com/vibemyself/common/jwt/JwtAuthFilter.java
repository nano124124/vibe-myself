package com.vibemyself.common.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibemyself.common.redis.RedisService;
import com.vibemyself.common.security.LoginUser;
import com.vibemyself.service.member.MemberAuthService;
import com.vibemyself.service.system.AdminAuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final MemberAuthService memberAuthService;
    private final AdminAuthService adminAuthService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String accessToken = resolveToken(request, "access_token");

        if (accessToken == null) {
            chain.doFilter(request, response);
            return;
        }

        if (!jwtProvider.isValid(accessToken)) {
            if (!jwtProvider.isExpired(accessToken)) {
                // 서명 자체가 유효하지 않음
                sendUnauthorized(response);
                return;
            }
            // 만료된 토큰 - Security Context 세팅 없이 통과 (permitAll 엔드포인트에서만 처리)
            Claims expiredClaims = jwtProvider.parseClaimsIgnoreExpiry(accessToken);
            String expiredId = expiredClaims.getSubject();
            String expiredType = expiredClaims.get("type", String.class);
            // 만료 정보만 추출하고 인증은 세팅하지 않음
            chain.doFilter(request, response);
            return;
        }

        Claims claims = jwtProvider.parseClaims(accessToken);
        String id = claims.getSubject();
        String type = claims.get("type", String.class);

        LoginUser loginUser = loadLoginUser(type, id);
        if (loginUser == null) {
            sendUnauthorized(response);
            return;
        }

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        chain.doFilter(request, response);
    }

    private LoginUser loadLoginUser(String type, String id) {
        String sessionKey = "session:" + type + ":" + id;
        String cached = redisService.get(sessionKey);

        if (cached != null) {
            try {
                return objectMapper.readValue(cached, LoginUser.class);
            } catch (Exception ignored) {
                // 역직렬화 실패 시 DB 재조회
            }
        }

        LoginUser loginUser = "member".equals(type)
                ? memberAuthService.loadUser(id)
                : adminAuthService.loadUser(id);

        if (loginUser == null) return null;

        try {
            redisService.save(sessionKey, objectMapper.writeValueAsString(loginUser), 3600L);
        } catch (Exception ignored) {}

        return loginUser;
    }

    private String resolveToken(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }

    private void sendUnauthorized(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\"}");
    }
}
