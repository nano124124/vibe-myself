package com.vibemyself.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum GoodsType {
    NORMAL("NORMAL", "일반상품"),
    EGIFT("EGIFT", "e쿠폰"),
    GIFT("GIFT", "사은품");

    private final String cd;
    private final String description;

    public static GoodsType fromCd(String cd) {
        return Arrays.stream(values())
                .filter(e -> e.cd.equals(cd))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 상품유형 코드: " + cd));
    }

    public static boolean isValid(String cd) {
        return Arrays.stream(values()).anyMatch(e -> e.cd.equals(cd));
    }
}