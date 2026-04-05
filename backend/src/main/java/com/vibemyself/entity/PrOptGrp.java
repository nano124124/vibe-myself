package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PrOptGrp extends CommonEntity {
    private String optGrpCd;
    private String optGrpNm;
    private int sortOrd;
    private String useYn;
}