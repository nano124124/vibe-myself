package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class StMenuBase extends CommonEntity {
    private Long menuNo;
    private Long parentMenuNo;
    private String menuNm;
    private String menuUrl;
    private int sortOrd;
}
