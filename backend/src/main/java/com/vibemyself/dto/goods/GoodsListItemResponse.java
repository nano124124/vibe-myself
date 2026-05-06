package com.vibemyself.dto.goods;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class GoodsListItemResponse {
    private String goodsNo;
    private String goodsNm;
    private String goodsTpCd;
    private String saleStatCd;
    private String ctgNm;
    private String brandNm;
    private BigDecimal salePrc;
    private String thumbImgUrl;
    private LocalDateTime regDtm;
}