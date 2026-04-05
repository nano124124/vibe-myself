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
public class PrDlvPolicy extends CommonEntity {
    private String dlvPolicyNo;
    private String dlvPolicyNm;
    private String dlvTpCd;
    private BigDecimal dlvAmt;
    private BigDecimal freeCondAmt;
    private BigDecimal rtnDlvAmt;
    private String useYn;
}