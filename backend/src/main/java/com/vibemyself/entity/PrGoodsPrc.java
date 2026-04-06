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
public class PrGoodsPrc extends CommonEntity {
    private String goodsNo;
    private String aplyFromDt;   // 적용시작일자 (yyyyMMdd)
    private String aplyToDt;     // 적용종료일자 (yyyyMMdd, 현재가격: '99991231')
    private BigDecimal salePrc;  // 판매가
    private BigDecimal normPrc;  // 정상가
    private BigDecimal suplyPrc; // 공급원가
    private BigDecimal mrgnRate; // 마진율 (%)
}
