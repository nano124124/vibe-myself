package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PrUnitBase extends CommonEntity {
    private String goodsNo;
    private int unitSeq;
    private BigDecimal addPrc;
    private int stockQty;
    private String useYn;
}
