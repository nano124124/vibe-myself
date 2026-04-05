package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PrBrandBase extends CommonEntity {
    private Long brandNo;
    private String brandNm;
    private String brandImgUrl;
    private String useYn;
}