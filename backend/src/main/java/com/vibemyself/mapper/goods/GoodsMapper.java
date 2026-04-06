package com.vibemyself.mapper.goods;

import com.vibemyself.entity.PrBrandBase;
import com.vibemyself.entity.PrDlvPolicy;
import com.vibemyself.entity.PrGoodsBase;
import com.vibemyself.entity.PrGoodsImg;
import com.vibemyself.entity.PrGoodsOpt;
import com.vibemyself.entity.PrGoodsPrc;
import com.vibemyself.entity.PrOptGrp;
import com.vibemyself.entity.PrOptItm;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GoodsMapper {

    PrBrandBase selectBrandByNo(Long brandNo);

    List<PrBrandBase> selectAllBrands();

    PrDlvPolicy selectDlvPolicyByNo(String dlvPolicyNo);

    List<PrDlvPolicy> selectAllDlvPolicies();

    PrOptGrp selectOptGrpByCd(String optGrpCd);

    List<PrOptGrp> selectAllOptGrps();

    PrOptItm selectOptItmByCds(@Param("optGrpCd") String optGrpCd,
                               @Param("optItmCd") String optItmCd);

    List<PrOptItm> selectOptItmsByGrpCd(String optGrpCd);

    void insertGoods(PrGoodsBase goods);

    void insertGoodsPrc(PrGoodsPrc goodsPrc);

    void insertGoodsImg(PrGoodsImg goodsImg);

    void insertGoodsOpt(PrGoodsOpt goodsOpt);
}