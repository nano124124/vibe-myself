package com.vibemyself.service.goods;

import com.vibemyself.common.storage.SupabaseStorageService;
import com.vibemyself.dto.goods.CreateGoodsRequest;
import com.vibemyself.global.exception.AppException;
import com.vibemyself.global.exception.ErrorCode;
import com.vibemyself.mapper.goods.CategoryMapper;
import com.vibemyself.mapper.goods.GoodsMapper;
import com.vibemyself.mapper.goods.UnitMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GoodsCreateServiceTest {

    @Mock CategoryMapper categoryMapper;
    @Mock GoodsMapper goodsMapper;
    @Mock UnitMapper unitMapper;
    @Mock SupabaseStorageService supabaseStorageService;
    @InjectMocks GoodsCreateService goodsCreateService;

    // ── calcMrgnRate ──────────────────────────────────────────────────────────

    @Test
    void calcMrgnRate_공급원가null_null반환() {
        assertThat(goodsCreateService.calcMrgnRate(new BigDecimal("10000"), null)).isNull();
    }

    @Test
    void calcMrgnRate_판매가0_null반환() {
        assertThat(goodsCreateService.calcMrgnRate(BigDecimal.ZERO, new BigDecimal("5000"))).isNull();
    }

    @Test
    void calcMrgnRate_정상케이스_37_5퍼센트() {
        // (10000 - 6250) / 10000 * 100 = 37.50
        BigDecimal result = goodsCreateService.calcMrgnRate(new BigDecimal("10000"), new BigDecimal("6250"));
        assertThat(result).isEqualByComparingTo("37.50");
    }

    @Test
    void calcMrgnRate_반올림_소수2자리() {
        // (10000 - 3333) / 10000 * 100 = 66.67
        BigDecimal result = goodsCreateService.calcMrgnRate(new BigDecimal("10000"), new BigDecimal("3333"));
        assertThat(result).isEqualByComparingTo("66.67");
    }

    @Test
    void calcMrgnRate_공급원가가판매가와동일_0퍼센트() {
        BigDecimal result = goodsCreateService.calcMrgnRate(new BigDecimal("10000"), new BigDecimal("10000"));
        assertThat(result).isEqualByComparingTo("0.00");
    }

    // ── validate: 음수 마진율 ──────────────────────────────────────────────────

    @Test
    void createGoods_공급원가가판매가초과_400예외() {
        CreateGoodsRequest request = new CreateGoodsRequest(
                "테스트상품", "NORMAL", 1L, null,
                new BigDecimal("10000"), null, new BigDecimal("12000"),
                null, null, null, "SALE", "DLV001", null, List.of()
        );

        assertThatThrownBy(() -> goodsCreateService.createGoods(request, List.of()))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NEGATIVE_MARGIN_RATE);
    }

    @Test
    void createGoods_공급원가가판매가와동일_예외없음_다음검증으로진행() {
        CreateGoodsRequest request = new CreateGoodsRequest(
                "테스트상품", "NORMAL", 1L, null,
                new BigDecimal("10000"), null, new BigDecimal("10000"),
                null, null, null, "SALE", "DLV001", null, List.of()
        );

        // 마진율 체크는 통과 → 다음 validate 단계(카테고리 조회)에서 예외
        assertThatThrownBy(() -> goodsCreateService.createGoods(request, List.of()))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);
    }
}