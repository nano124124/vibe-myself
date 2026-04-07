package com.vibemyself.service.goods;

import com.vibemyself.common.storage.SupabaseStorageService;
import com.vibemyself.common.util.SecurityUtils;
import com.vibemyself.dto.goods.CreateGoodsRequest;
import com.vibemyself.dto.goods.UnitOptRequest;
import com.vibemyself.dto.goods.UnitRequest;
import com.vibemyself.entity.PrGoodsBase;
import com.vibemyself.entity.PrGoodsImg;
import com.vibemyself.entity.PrGoodsOpt;
import com.vibemyself.entity.PrGoodsPrc;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsCreateService {

    private static final String PRC_APLY_TO_DT_INFINITY = "99991231";
    private static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final LocalDateTime SALE_END_DTM_DEFAULT = LocalDateTime.of(2999, 12, 31, 0, 0, 0);

    private final CategoryMapper categoryMapper;
    private final GoodsMapper goodsMapper;
    private final UnitMapper unitMapper;
    private final SupabaseStorageService supabaseStorageService;

    @Transactional
    public String createGoods(CreateGoodsRequest request, List<MultipartFile> images) {
        validate(request);

        String userId = SecurityUtils.currentUserId();

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

        List<String> imgUrls = images.stream()
                .map(file -> supabaseStorageService.upload(file, goods.getGoodsNo()))
                .toList();
        insertImages(goods.getGoodsNo(), imgUrls, userId);

        List<String> optGrpCds = request.optGrpCds() != null ? request.optGrpCds() : Collections.emptyList();
        List<UnitRequest> units = request.units() != null ? request.units() : Collections.emptyList();
        insertGoodsOpts(goods.getGoodsNo(), optGrpCds, userId);
        insertUnits(goods.getGoodsNo(), units, userId);

        return goods.getGoodsNo();
    }

    private void validate(CreateGoodsRequest request) {
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
        List<String> optGrpCds = request.optGrpCds() != null ? request.optGrpCds() : Collections.emptyList();
        for (String optGrpCd : optGrpCds) {
            if (goodsMapper.selectOptGrpByCd(optGrpCd) == null) {
                throw new AppException(ErrorCode.OPT_GRP_NOT_FOUND);
            }
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

    private BigDecimal calcMrgnRate(BigDecimal salePrc, BigDecimal suplyPrc) {
        if (suplyPrc == null || salePrc.compareTo(BigDecimal.ZERO) == 0) {
            return null;
        }
        return salePrc.subtract(suplyPrc)
                .divide(salePrc, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

}
