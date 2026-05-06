package com.vibemyself.service.goods;

import com.vibemyself.common.response.PageResponse;
import com.vibemyself.dto.goods.GoodsListItemResponse;
import com.vibemyself.dto.goods.GoodsListRequest;
import com.vibemyself.mapper.goods.GoodsMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GoodsListServiceTest {

    @Mock GoodsMapper goodsMapper;
    @InjectMocks GoodsListService goodsListService;

    @Test
    void getGoodsList_결과없음_빈페이지반환() {
        GoodsListRequest request = new GoodsListRequest(null, null, null, 0, 20);
        when(goodsMapper.selectGoodsList(any())).thenReturn(List.of());
        when(goodsMapper.countGoodsList(any())).thenReturn(0L);

        PageResponse<GoodsListItemResponse> result = goodsListService.getGoodsList(request);

        assertThat(result.content()).isEmpty();
        assertThat(result.totalElements()).isZero();
        assertThat(result.totalPages()).isZero();
    }

    @Test
    void getGoodsList_2페이지_offset_계산() {
        GoodsListRequest request = new GoodsListRequest(null, null, null, 1, 20);
        when(goodsMapper.selectGoodsList(any())).thenReturn(List.of());
        when(goodsMapper.countGoodsList(any())).thenReturn(21L);

        PageResponse<GoodsListItemResponse> result = goodsListService.getGoodsList(request);

        assertThat(result.page()).isEqualTo(1);
        assertThat(result.totalPages()).isEqualTo(2);
    }

    @Test
    void getGoodsList_totalPages_올림계산() {
        GoodsListRequest request = new GoodsListRequest(null, null, null, 0, 20);
        when(goodsMapper.selectGoodsList(any())).thenReturn(List.of());
        when(goodsMapper.countGoodsList(any())).thenReturn(21L);

        PageResponse<GoodsListItemResponse> result = goodsListService.getGoodsList(request);

        assertThat(result.totalPages()).isEqualTo(2);
    }
}