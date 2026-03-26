package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StMenuBase {
    private Long menuNo;
    private Long parentMenuNo;
    private String menuNm;
    private String menuUrl;
    private int sortOrd;
}
