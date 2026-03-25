package com.vibemyself.model.member;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private String mbrNo;
    private String loginId;
    private String loginPwd;
    private String mbrNm;
    private String email;
    private String grdCd;
    private String mbrStatCd;
}
