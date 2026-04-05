package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PrGoodsBase extends CommonEntity {
    @Setter // MyBatis selectKey 주입용
    private String goodsNo;
    private String goodsNm;
    private String goodsTpCd;
    private BigDecimal salePrc;
    private Long ctgNo;
    private Long brandNo;
    private String saleStatCd;
    private String dlvPolicyNo;
    private String goodsDesc;
}