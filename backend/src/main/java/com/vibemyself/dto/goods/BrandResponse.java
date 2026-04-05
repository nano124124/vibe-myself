package com.vibemyself.dto.goods;

import com.vibemyself.entity.PrBrandBase;
import lombok.Getter;
import lombok.AllArgsConstructor;

@Getter
@AllArgsConstructor
public class BrandResponse {
    private final Long brandNo;
    private final String brandNm;
    private final String brandImgUrl;

    public static BrandResponse of(PrBrandBase brand) {
        return new BrandResponse(brand.getBrandNo(), brand.getBrandNm(), brand.getBrandImgUrl());
    }
}