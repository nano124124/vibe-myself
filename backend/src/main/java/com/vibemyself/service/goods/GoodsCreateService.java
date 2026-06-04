package com.vibemyself.service.goods;

import com.vibemyself.common.storage.SupabaseStorageService;
import com.vibemyself.common.util.SecurityUtils;
import com.vibemyself.dto.goods.CreateGoodsRequest;
import com.vibemyself.dto.goods.UnitOptRequest;
import com.vibemyself.enums.GoodsSaleStatus;
import com.vibemyself.enums.GoodsType;
import com.vibemyself.global.exception.AppException;
import com.vibemyself.global.exception.ErrorCode;
import com.vibemyself.mapper.goods.CategoryMapper;
import com.vibemyself.mapper.goods.GoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoodsCreateService {

    private final CategoryMapper categoryMapper;
    private final GoodsMapper goodsMapper;
    private final SupabaseStorageService supabaseStorageService;
    private final GoodsCreateTxService goodsCreateTxService;

    public String createGoods(CreateGoodsRequest request, List<MultipartFile> images) {
        validate(request);

        String userId = SecurityUtils.currentUserId();
        String uploadKey = java.util.UUID.randomUUID().toString();
        List<String> uploadedUrls = new ArrayList<>();

        try {
            images.stream()
                    .map(file -> supabaseStorageService.upload(file, uploadKey))
                    .forEach(uploadedUrls::add);

            return goodsCreateTxService.saveGoods(request, uploadedUrls, userId);

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
        validateAllOptItms(request);
    }

    private void validateAllOptItms(CreateGoodsRequest request) {
        if (request.units() == null) {
            return;
        }
        List<UnitOptRequest> allOptItms = request.units().stream()
                .flatMap(u -> u.optItms().stream())
                .distinct()
                .collect(Collectors.toList());
        if (allOptItms.isEmpty()) {
            return;
        }
        if (goodsMapper.countValidOptItmPairs(allOptItms) != allOptItms.size()) {
            throw new AppException(ErrorCode.OPT_ITM_NOT_FOUND);
        }
    }

}
