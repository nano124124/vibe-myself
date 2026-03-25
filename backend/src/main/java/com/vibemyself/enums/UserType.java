package com.vibemyself.enums;

import lombok.Getter;

/**
 * 사용자 유형 (JWT claim / Redis key 구분자)
 * DB 공통코드와 무관한 애플리케이션 내부 상수
 */
@Getter
public enum UserType {
    MEMBER("member"),
    ADMIN("admin");

    private final String value;

    UserType(String value) {
        this.value = value;
    }

}
