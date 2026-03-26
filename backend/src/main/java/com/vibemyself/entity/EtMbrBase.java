package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EtMbrBase {
    private String mbrNo;
    private String loginId;
    private String loginPwd;
    private String mbrNm;
    private String email;
    private String grdCd;
    private String mbrStatCd;
}
