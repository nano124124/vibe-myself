package com.vibemyself.service.goods;

import com.vibemyself.dto.goods.OptGrpResponse;
import com.vibemyself.dto.goods.OptItmResponse;
import com.vibemyself.mapper.goods.GoodsMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OptGroupQueryService {

    private final GoodsMapper goodsMapper;

    @Transactional(readOnly = true)
    public List<OptGrpResponse> getOptGroups() {
        return goodsMapper.selectAllOptGrps().stream()
                .map(grp -> {
                    List<OptItmResponse> items = goodsMapper.selectOptItmsByGrpCd(grp.getOptGrpCd())
                            .stream().map(OptItmResponse::of).toList();
                    return OptGrpResponse.of(grp, items);
                })
                .toList();
    }
}