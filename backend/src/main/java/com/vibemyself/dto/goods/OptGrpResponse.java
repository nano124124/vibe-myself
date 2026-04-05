package com.vibemyself.dto.goods;

import com.vibemyself.entity.PrOptGrp;
import lombok.Getter;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
public class OptGrpResponse {
    private final String optGrpCd;
    private final String optGrpNm;
    private final int sortOrd;
    private final List<OptItmResponse> items;

    public static OptGrpResponse of(PrOptGrp grp, List<OptItmResponse> items) {
        return new OptGrpResponse(grp.getOptGrpCd(), grp.getOptGrpNm(), grp.getSortOrd(), items);
    }
}