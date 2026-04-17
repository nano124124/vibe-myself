package com.vibemyself.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum YesNo {
    Y("Y"),
    N("N");

    private final String cd;
}
