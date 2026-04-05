package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class EtMbrBase extends CommonEntity {
    private String mbrNo;
    private String loginId;
    private String loginPwd;
    private String mbrNm;
    private String email;
    private String grdCd;
    private String mbrStatCd;
}
