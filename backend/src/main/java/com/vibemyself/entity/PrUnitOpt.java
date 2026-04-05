package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PrUnitOpt extends CommonEntity {
    private String goodsNo;
    private int unitSeq;
    private String optGrpCd;
    private String optItmCd;
}
