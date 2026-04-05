package com.vibemyself.service.goods;

import com.vibemyself.dto.goods.BrandResponse;
import com.vibemyself.mapper.goods.GoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BrandQueryService {

    private final GoodsMapper goodsMapper;

    @Transactional(readOnly = true)
    public List<BrandResponse> getBrands() {
        return goodsMapper.selectAllBrands().stream()
                .map(BrandResponse::of)
                .toList();
    }
}