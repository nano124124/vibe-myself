package com.vibemyself.enums;

import lombok.Getter;

import java.util.Arrays;

/**
 * 관리자 역할 코드 (DB: ST_CODE_DTL / ROLE_CD)
 * USER는 DB에 없는 회원용 Spring Security role
 */
@Getter
public enum RoleCode {
    SUPER("SUPER"),
    ADMIN("ADMIN"),
    OPS("OPS"),
    USER("USER");

    private final String code;

    RoleCode(String code) {
        this.code = code;
    }

    public String toSpringRole() {
        return "ROLE_" + code;
    }

    public static RoleCode from(String code) {
        return Arrays.stream(values())
                .filter(r -> r.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 역할 코드: " + code));
    }
}
