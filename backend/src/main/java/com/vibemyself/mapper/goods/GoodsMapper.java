package com.vibemyself.mapper.goods;

import com.vibemyself.dto.goods.GoodsListItemResponse;
import com.vibemyself.dto.goods.GoodsListRequest;
import com.vibemyself.dto.goods.UnitOptRequest;
import com.vibemyself.entity.PrBrandBase;
import com.vibemyself.entity.PrDlvPolicy;
import com.vibemyself.entity.PrGoodsBase;
import com.vibemyself.entity.PrGoodsImg;
import com.vibemyself.entity.PrGoodsPrc;
import com.vibemyself.entity.PrGoodsTag;
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

    String nextGoodsNo();

    int countValidOptItmPairs(@Param("pairs") List<UnitOptRequest> pairs);

    void insertGoods(PrGoodsBase goods);

    void insertGoodsPrc(PrGoodsPrc goodsPrc);

    void insertGoodsImg(PrGoodsImg goodsImg);

    void insertGoodsTag(PrGoodsTag goodsTag);

    List<GoodsListItemResponse> selectGoodsList(GoodsListRequest request);

    long countGoodsList(GoodsListRequest request);
}