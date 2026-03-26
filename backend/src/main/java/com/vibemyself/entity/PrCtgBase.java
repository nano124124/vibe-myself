package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrCtgBase {
    @Setter                  // MyBatis useGeneratedKeys 주입을 위해 필요
    private Long ctgNo;
    private Long upCtgNo;
    private String ctgLvl;   // "1", "2", "3"
    private String ctgNm;
    private int sortOrd;
    private String useYn;
}
