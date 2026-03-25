package com.vibemyself.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseCookie;

public class CookieUtils {

    private CookieUtils() {}

    public static ResponseCookie buildCookie(String name, String value, int maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .sameSite("Lax")
                .maxAge(maxAge)
                .path("/")
                .build();
    }

    public static ResponseCookie clearCookie(String name) {
        return buildCookie(name, "", 0);
    }

    // TODO: 프로덕션에서는 .secure(true) 추가 필요. 현재는 로컬 개발용으로 false.

    public static String resolveToken(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (cookieName.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}
