package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PrCtgBase extends CommonEntity {
    @Setter // MyBatis useGeneratedKeys 주입용
    private Long ctgNo;
    private Long upCtgNo;
    private String ctgLvl;
    private String ctgNm;
    private int sortOrd;
    private String useYn;
}