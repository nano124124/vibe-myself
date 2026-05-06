package com.vibemyself.service.goods;

import com.vibemyself.common.response.PageResponse;
import com.vibemyself.dto.goods.GoodsListItemResponse;
import com.vibemyself.dto.goods.GoodsListRequest;
import com.vibemyself.mapper.goods.GoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GoodsListService {

    private final GoodsMapper goodsMapper;

    @Transactional(readOnly = true)
    public PageResponse<GoodsListItemResponse> getGoodsList(GoodsListRequest request) {
        List<GoodsListItemResponse> content = goodsMapper.selectGoodsList(request);
        long total = goodsMapper.countGoodsList(request);
        return PageResponse.of(content, request.page(), request.size(), total);
    }
}