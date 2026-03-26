package com.vibemyself.dto.goods;

import com.vibemyself.entity.PrCtgBase;
import lombok.Getter;

import java.util.List;

@Getter
public class CategoryResponse {
    private final Long ctgNo;
    private final Long upCtgNo;
    private final String ctgLvl;
    private final String ctgNm;
    private final int sortOrd;
    private final String useYn;
    private final List<CategoryResponse> children;

    private CategoryResponse(PrCtgBase category, List<CategoryResponse> children) {
        this.ctgNo = category.getCtgNo();
        this.upCtgNo = category.getUpCtgNo();
        this.ctgLvl = category.getCtgLvl();
        this.ctgNm = category.getCtgNm();
        this.sortOrd = category.getSortOrd();
        this.useYn = category.getUseYn();
        this.children = children;
    }

    public static CategoryResponse of(PrCtgBase category, List<CategoryResponse> children) {
        return new CategoryResponse(category, children);
    }
}
