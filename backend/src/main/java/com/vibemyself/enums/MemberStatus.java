package com.vibemyself.enums;

import lombok.Getter;

/**
 * 회원 상태 코드 (DB: ST_CODE_DTL / MBR_STAT_CD)
 */
@Getter
public enum MemberStatus {
    NORMAL("NORMAL"),
    STOP("STOP"),
    WITHDRAW("WITHDRAW");

    private final String code;

    MemberStatus(String code) {
        this.code = code;
    }

}
