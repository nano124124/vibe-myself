package com.vibemyself.mapper.goods;

import com.vibemyself.entity.PrBrandBase;
import com.vibemyself.entity.PrDlvPolicy;
import com.vibemyself.entity.PrGoodsBase;
import com.vibemyself.entity.PrGoodsImg;
import com.vibemyself.entity.PrGoodsOpt;
import com.vibemyself.entity.PrOptGrp;
import com.vibemyself.entity.PrOptItm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface GoodsMapper {

    PrBrandBase selectBrandByNo(Long brandNo);

    PrDlvPolicy selectDlvPolicyByNo(String dlvPolicyNo);

    PrOptGrp selectOptGrpByCd(String optGrpCd);

    PrOptItm selectOptItmByCds(@Param("optGrpCd") String optGrpCd,
                               @Param("optItmCd") String optItmCd);

    void insertGoods(PrGoodsBase goods);

    void insertGoodsImg(PrGoodsImg goodsImg);

    void insertGoodsOpt(PrGoodsOpt goodsOpt);
}