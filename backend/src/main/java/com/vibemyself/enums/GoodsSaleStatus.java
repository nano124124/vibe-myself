package com.vibemyself.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum GoodsSaleStatus {
    SALE("SALE", "판매중"),
    STOP("STOP", "판매중지");

    private final String cd;
    private final String description;

    public static GoodsSaleStatus fromCd(String cd) {
        return Arrays.stream(values())
                .filter(e -> e.cd.equals(cd))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 판매상태 코드: " + cd));
    }

    public static boolean isValid(String cd) {
        return Arrays.stream(values()).anyMatch(e -> e.cd.equals(cd));
    }
}
