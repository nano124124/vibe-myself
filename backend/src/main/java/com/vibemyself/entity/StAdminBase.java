package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StAdminBase {
    private String loginId;
    private String loginPwd;
    private String adminNm;
    private String roleCd;
    private String useYn;
}
