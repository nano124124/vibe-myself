package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PrGoodsOpt extends CommonEntity {
    private String goodsNo;
    private String optGrpCd;
    private int sortOrd;
}