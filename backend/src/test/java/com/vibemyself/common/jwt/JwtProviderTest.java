package com.vibemyself.common.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Base64;

import static org.assertj.core.api.Assertions.*;

class JwtProviderTest {

    private JwtProvider jwtProvider;
    private static final String SECRET = Base64.getEncoder()
            .encodeToString("test-secret-key-for-local-dev-minimum-32-chars".getBytes());

    @BeforeEach
    void setUp() {
        jwtProvider = new JwtProvider(SECRET, 3600000L, 1209600000L);
    }

    @Test
    void generateAccessToken_클레임포함확인() {
        String token = jwtProvider.generateAccessToken("1", "ROLE_USER", "member");
        Claims claims = jwtProvider.parseClaims(token);
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("role")).isEqualTo("ROLE_USER");
        assertThat(claims.get("type")).isEqualTo("member");
    }

    @Test
    void generateRefreshToken_클레임포함확인() {
        String token = jwtProvider.generateRefreshToken("1", "member");
        Claims claims = jwtProvider.parseClaims(token);
        assertThat(claims.getSubject()).isEqualTo("1");
        assertThat(claims.get("type")).isEqualTo("member");
    }

    @Test
    void isValid_만료토큰_false반환() throws InterruptedException {
        JwtProvider shortLived = new JwtProvider(SECRET, 1L, 1L);
        String token = shortLived.generateAccessToken("1", "ROLE_USER", "member");
        Thread.sleep(50);
        assertThat(shortLived.isValid(token)).isFalse();
    }

    @Test
    void isExpired_만료토큰_true반환() throws InterruptedException {
        JwtProvider shortLived = new JwtProvider(SECRET, 1L, 1L);
        String token = shortLived.generateAccessToken("1", "ROLE_USER", "member");
        Thread.sleep(50);
        assertThat(shortLived.isExpired(token)).isTrue();
    }

    @Test
    void parseClaimsIgnoreExpiry_만료토큰도클레임반환() throws InterruptedException {
        JwtProvider shortLived = new JwtProvider(SECRET, 1L, 1L);
        String token = shortLived.generateAccessToken("1", "ROLE_USER", "member");
        Thread.sleep(50);
        Claims claims = shortLived.parseClaimsIgnoreExpiry(token);
        assertThat(claims.getSubject()).isEqualTo("1");
    }

    @Test
    void isValid_잘못된서명_false반환() {
        String invalidToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.invalid";
        assertThat(jwtProvider.isValid(invalidToken)).isFalse();
    }
}
