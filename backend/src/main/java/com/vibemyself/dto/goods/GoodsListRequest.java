package com.vibemyself.dto.goods;

public record GoodsListRequest(
        String goodsNm,
        String saleStatCd,
        Long ctgNo,
        int page,
        int size
) {
    public GoodsListRequest {
        if (page < 0) page = 0;
        if (size <= 0) size = 20;
        if (size > 100) size = 100;
    }

    public int offset() {
        return page * size;
    }
}