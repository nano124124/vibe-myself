package com.vibemyself.dto.goods;

import com.vibemyself.entity.PrOptItm;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class OptItmResponse {
    private final String optGrpCd;
    private final String optItmCd;
    private final String optItmNm;
    private final int sortOrd;

    public static OptItmResponse of(PrOptItm item) {
        return new OptItmResponse(item.getOptGrpCd(), item.getOptItmCd(), item.getOptItmNm(), item.getSortOrd());
    }
}