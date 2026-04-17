package com.vibemyself.controller.goods;

import com.vibemyself.common.response.ApiResponse;
import com.vibemyself.dto.goods.CreateGoodsRequest;
import com.vibemyself.dto.goods.CreateGoodsResponse;
import com.vibemyself.service.goods.GoodsCreateService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/admin/goods")
@RequiredArgsConstructor
public class GoodsController {

    private final GoodsCreateService goodsCreateService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<CreateGoodsResponse>> createGoods(
            @RequestPart("data") @Valid CreateGoodsRequest data,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        List<MultipartFile> imageList = images != null ? images : Collections.emptyList();
        String goodsNo = goodsCreateService.createGoods(data, imageList);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new CreateGoodsResponse(goodsNo)));
    }
}
