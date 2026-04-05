package com.vibemyself.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PrGoodsImg extends CommonEntity {
    private String goodsNo;
    private int imgSeq;
    private String imgUrl;
    private int sortOrd;
}