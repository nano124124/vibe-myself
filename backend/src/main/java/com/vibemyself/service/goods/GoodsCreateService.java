package com.vibemyself.service.goods;

import com.vibemyself.common.storage.SupabaseStorageService;
import com.vibemyself.common.util.SecurityUtils;
import com.vibemyself.dto.goods.CreateGoodsRequest;
import com.vibemyself.dto.goods.UnitOptRequest;
import com.vibemyself.dto.goods.UnitRequest;
import com.vibemyself.entity.PrGoodsBase;
import com.vibemyself.entity.PrGoodsImg;
import com.vibemyself.entity.PrGoodsPrc;
import com.vibemyself.entity.PrGoodsTag;
import com.vibemyself.entity.PrUnitBase;
import com.vibemyself.entity.PrUnitOpt;
import com.vibemyself.enums.GoodsSaleStatus;
import com.vibemyself.enums.GoodsType;
import com.vibemyself.enums.YesNo;
import com.vibemyself.global.exception.AppException;
import com.vibemyself.global.exception.ErrorCode;
import com.vibemyself.mapper.goods.CategoryMapper;
import com.vibemyself.mapper.goods.GoodsMapper;
import com.vibemyself.mapper.goods.UnitMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.vibemyself.service.goods.GoodsConstants.*;

@Service
@RequiredArgsConstructor
public class GoodsCreateService {

    private final CategoryMapper categoryMapper;
    private final GoodsMapper goodsMapper;
    private final UnitMapper unitMapper;
    private final SupabaseStorageService supabaseStorageService;

    @Transactional
    public String createGoods(CreateGoodsRequest request, List<MultipartFile> images) {
        validate(request);

        String userId = SecurityUtils.currentUserId();
        List<String> uploadedUrls = new ArrayList<>();

        try {
            PrGoodsBase goods = PrGoodsBase.builder()
                    .goodsNm(request.goodsNm())
                    .goodsTpCd(GoodsType.fromCd(request.goodsTpCd()).getCd())
                    .ctgNo(request.ctgNo())
                    .brandNo(request.brandNo())
                    .saleStatCd(GoodsSaleStatus.fromCd(request.saleStatCd()).getCd())
                    .dlvPolicyNo(request.dlvPolicyNo())
                    .goodsDesc(request.goodsDesc())
                    .saleStartDtm(request.saleStartDtm())
                    .saleEndDtm(request.saleEndDtm() != null ? request.saleEndDtm() : SALE_END_DTM_DEFAULT)
                    .regId(userId)
                    .modId(userId)
                    .build();
            goodsMapper.insertGoods(goods);

            goodsMapper.insertGoodsPrc(PrGoodsPrc.builder()
                    .goodsNo(goods.getGoodsNo())
                    .aplyFromDt(LocalDate.now().format(DT_FORMATTER))
                    .aplyToDt(PRC_APLY_TO_DT_INFINITY)
                    .salePrc(request.salePrc())
                    .normPrc(request.normPrc())
                    .suplyPrc(request.suplyPrc())
                    .mrgnRate(calcMrgnRate(request.salePrc(), request.suplyPrc()))
                    .regId(userId)
                    .modId(userId)
                    .build());

            images.stream()
                    .map(file -> supabaseStorageService.upload(file, goods.getGoodsNo()))
                    .forEach(uploadedUrls::add);
            insertImages(goods.getGoodsNo(), uploadedUrls, userId);

            List<UnitRequest> units = request.units() != null ? request.units() : Collections.emptyList();
            List<String> tagNms = request.tagNms() != null ? request.tagNms() : Collections.emptyList();
            insertUnits(goods.getGoodsNo(), units, userId);
            insertGoodsTags(goods.getGoodsNo(), tagNms, userId);

            return goods.getGoodsNo();

        } catch (Exception e) {
            supabaseStorageService.deleteAll(uploadedUrls);
            throw e;
        }
    }

    private void validate(CreateGoodsRequest request) {
        if (request.suplyPrc() != null && request.suplyPrc().compareTo(request.salePrc()) > 0) {
            throw new AppException(ErrorCode.NEGATIVE_MARGIN_RATE);
        }
        if (!GoodsType.isValid(request.goodsTpCd())) {
            throw new AppException(ErrorCode.INVALID_GOODS_TYPE_CD);
        }
        if (!GoodsSaleStatus.isValid(request.saleStatCd())) {
            throw new AppException(ErrorCode.INVALID_SALE_STAT_CD);
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
        List<UnitRequest> units = request.units() != null ? request.units() : Collections.emptyList();
        for (UnitRequest unit : units) {
            validateUnitOptItems(unit);
        }
    }

    private void validateUnitOptItems(UnitRequest unit) {
        for (UnitOptRequest optItm : unit.optItms()) {
            if (goodsMapper.selectOptItmByCds(optItm.optGrpCd(), optItm.optItmCd()) == null) {
                throw new AppException(ErrorCode.OPT_ITM_NOT_FOUND);
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

    private void insertUnits(String goodsNo, List<UnitRequest> units, String userId) {
        for (int i = 0; i < units.size(); i++) {
            UnitRequest unitReq = units.get(i);
            int unitSeq = i + 1;

            unitMapper.insertUnit(PrUnitBase.builder()
                    .goodsNo(goodsNo)
                    .unitSeq(unitSeq)
                    .addPrc(unitReq.addPrc())
                    .stockQty(unitReq.stockQty())
                    .useYn(YesNo.Y.getCd())
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
                        .modId(userId)
                        .build());
            }
        }
    }

    private void insertGoodsTags(String goodsNo, List<String> tagNms, String userId) {
        for (int i = 0; i < tagNms.size(); i++) {
            goodsMapper.insertGoodsTag(PrGoodsTag.builder()
                    .goodsNo(goodsNo)
                    .tagSeq(i + 1)
                    .tagNm(tagNms.get(i))
                    .regId(userId)
                    .modId(userId)
                    .build());
        }
    }

    BigDecimal calcMrgnRate(BigDecimal salePrc, BigDecimal suplyPrc) {
        if (suplyPrc == null || salePrc.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return salePrc.subtract(suplyPrc)
                .divide(salePrc, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

}
