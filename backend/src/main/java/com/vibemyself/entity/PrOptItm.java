package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PrOptItm extends CommonEntity {
    private String optGrpCd;
    private String optItmCd;
    private String optItmNm;
    private int sortOrd;
    private String useYn;
}