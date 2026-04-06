package com.vibemyself.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CodeGroup {
    GOODS_TP("GOODS_TP"),
    SALE_STAT("SALE_STAT");

    private final String cd;
}
