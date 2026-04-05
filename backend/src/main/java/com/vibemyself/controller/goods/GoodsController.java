package com.vibemyself.controller.goods;

import com.vibemyself.common.response.ApiResponse;
import com.vibemyself.dto.goods.CreateGoodsRequest;
import com.vibemyself.dto.goods.CreateGoodsResponse;
import com.vibemyself.service.goods.GoodsCreateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsCreateService goodsCreateService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateGoodsResponse>> createGoods(
            @Valid @RequestBody CreateGoodsRequest request) {
        String goodsNo = goodsCreateService.createGoods(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new CreateGoodsResponse(goodsNo)));
    }
}