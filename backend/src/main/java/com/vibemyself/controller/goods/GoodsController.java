package com.vibemyself.controller.goods;

import com.vibemyself.common.response.ApiResponse;
import com.vibemyself.common.response.PageResponse;
import com.vibemyself.dto.goods.CreateGoodsRequest;
import com.vibemyself.dto.goods.CreateGoodsResponse;
import com.vibemyself.dto.goods.GoodsListItemResponse;
import com.vibemyself.dto.goods.GoodsListRequest;
import com.vibemyself.service.goods.GoodsCreateService;
import com.vibemyself.service.goods.GoodsListService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
    private final GoodsListService goodsListService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<GoodsListItemResponse>>> getGoodsList(
            @RequestParam(defaultValue = "") String goodsNm,
            @RequestParam(defaultValue = "") String saleStatCd,
            @RequestParam(required = false) Long ctgNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        GoodsListRequest request = new GoodsListRequest(
                goodsNm.isBlank() ? null : goodsNm,
                saleStatCd.isBlank() ? null : saleStatCd,
                ctgNo, page, size);
        return ResponseEntity.ok(ApiResponse.ok(goodsListService.getGoodsList(request)));
    }

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
