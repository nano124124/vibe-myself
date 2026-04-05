package com.vibemyself.service.goods;

import com.vibemyself.common.security.LoginUser;
import com.vibemyself.dto.goods.CreateGoodsRequest;
import com.vibemyself.dto.goods.UnitOptRequest;
import com.vibemyself.dto.goods.UnitRequest;
import com.vibemyself.entity.PrGoodsBase;
import com.vibemyself.entity.PrGoodsImg;
import com.vibemyself.entity.PrGoodsOpt;
import com.vibemyself.entity.PrUnitBase;
import com.vibemyself.entity.PrUnitOpt;
import com.vibemyself.enums.GoodsSaleStatus;
import com.vibemyself.enums.GoodsType;
import com.vibemyself.global.exception.AppException;
import com.vibemyself.global.exception.ErrorCode;
import com.vibemyself.mapper.goods.CategoryMapper;
import com.vibemyself.mapper.goods.GoodsMapper;
import com.vibemyself.mapper.goods.UnitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsCreateService {

    private static final int IMG_MAX_COUNT = 5;

    private final CategoryMapper categoryMapper;
    private final GoodsMapper goodsMapper;
    private final UnitMapper unitMapper;

    @Transactional
    public String createGoods(CreateGoodsRequest request) {
        validate(request);

        String userId = currentUserId();

        PrGoodsBase goods = PrGoodsBase.builder()
                .goodsNm(request.goodsNm())
                .goodsTpCd(GoodsType.fromCd(request.goodsTpCd()).getCd())
                .salePrc(request.salePrc())
                .ctgNo(request.ctgNo())
                .brandNo(request.brandNo())
                .saleStatCd(GoodsSaleStatus.fromCd(request.saleStatCd()).getCd())
                .dlvPolicyNo(request.dlvPolicyNo())
                .goodsDesc(request.goodsDesc())
                .regId(userId)
                .modId(userId)
                .build();
        goodsMapper.insertGoods(goods);

        insertImages(goods.getGoodsNo(), request.imgUrls(), userId);
        insertGoodsOpts(goods.getGoodsNo(), request.optGrpCds(), userId);
        insertUnits(goods.getGoodsNo(), request.units(), userId);

        return goods.getGoodsNo();
    }

    private void validate(CreateGoodsRequest request) {
        if (!CollectionUtils.isEmpty(request.imgUrls()) && request.imgUrls().size() > IMG_MAX_COUNT) {
            throw new AppException(ErrorCode.GOODS_IMG_LIMIT_EXCEEDED);
        }
        if (categoryMapper.selectByCtgNo(request.ctgNo()) == null) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        if (request.brandNo() != null && goodsMapper.selectBrandByNo(request.brandNo()) == null) {
            throw new AppException(ErrorCode.BRAND_NOT_FOUND);
        }
        if (goodsMapper.selectDlvPolicyByNo(request.dlvPolicyNo()) == null) {
            throw new AppException(ErrorCode.DLV_POLICY_NOT_FOUND);
        }
        for (String optGrpCd : request.optGrpCds()) {
            if (goodsMapper.selectOptGrpByCd(optGrpCd) == null) {
                throw new AppException(ErrorCode.OPT_GRP_NOT_FOUND);
            }
        }
        for (UnitRequest unit : request.units()) {
            for (UnitOptRequest optItm : unit.optItms()) {
                if (goodsMapper.selectOptItmByCds(optItm.optGrpCd(), optItm.optItmCd()) == null) {
                    throw new AppException(ErrorCode.OPT_ITM_NOT_FOUND);
                }
            }
        }
    }

    private void insertImages(String goodsNo, List<String> imgUrls, String userId) {
        if (CollectionUtils.isEmpty(imgUrls)) {
            return;
        }
        for (int i = 0; i < imgUrls.size(); i++) {
            goodsMapper.insertGoodsImg(PrGoodsImg.builder()
                    .goodsNo(goodsNo)
                    .imgSeq(i + 1)
                    .imgUrl(imgUrls.get(i))
                    .sortOrd(i + 1)
                    .regId(userId)
                    .modId(userId)
                    .build());
        }
    }

    private void insertGoodsOpts(String goodsNo, List<String> optGrpCds, String userId) {
        for (int i = 0; i < optGrpCds.size(); i++) {
            goodsMapper.insertGoodsOpt(PrGoodsOpt.builder()
                    .goodsNo(goodsNo)
                    .optGrpCd(optGrpCds.get(i))
                    .sortOrd(i + 1)
                    .regId(userId)
                    .modId(userId)
                    .build());
        }
    }

    private void insertUnits(String goodsNo, List<UnitRequest> units, String userId) {
        for (int i = 0; i < units.size(); i++) {
            UnitRequest unitReq = units.get(i);
            int unitSeq = i + 1;

            unitMapper.insertUnit(PrUnitBase.builder()
                    .goodsNo(goodsNo)
                    .unitSeq(unitSeq)
                    .addPrc(unitReq.addPrc())
                    .stockQty(unitReq.stockQty())
                    .useYn("Y")
                    .regId(userId)
                    .modId(userId)
                    .build());

            for (UnitOptRequest optItm : unitReq.optItms()) {
                unitMapper.insertUnitOpt(PrUnitOpt.builder()
                        .goodsNo(goodsNo)
                        .unitSeq(unitSeq)
                        .optGrpCd(optItm.optGrpCd())
                        .optItmCd(optItm.optItmCd())
                        .regId(userId)
                        .build());
            }
        }
    }

    private String currentUserId() {
        LoginUser loginUser = (LoginUser) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return loginUser.getLoginId();
    }
}
