package com.vibemyself.dto.goods;

import com.vibemyself.entity.PrDlvPolicy;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class DlvPolicyResponse {
    private final String dlvPolicyNo;
    private final String dlvPolicyNm;
    private final String dlvTpCd;
    private final BigDecimal dlvAmt;

    public static DlvPolicyResponse of(PrDlvPolicy policy) {
        return new DlvPolicyResponse(
                policy.getDlvPolicyNo(),
                policy.getDlvPolicyNm(),
                policy.getDlvTpCd(),
                policy.getDlvAmt()
        );
    }
}