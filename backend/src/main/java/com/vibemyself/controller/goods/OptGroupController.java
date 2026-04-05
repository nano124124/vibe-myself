package com.vibemyself.controller.goods;

import com.vibemyself.common.response.ApiResponse;
import com.vibemyself.dto.goods.OptGrpResponse;
import com.vibemyself.service.goods.OptGroupQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/goods/opt-groups")
@RequiredArgsConstructor
public class OptGroupController {

    private final OptGroupQueryService optGroupQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<OptGrpResponse>>> getOptGroups() {
        return ResponseEntity.ok(ApiResponse.ok(optGroupQueryService.getOptGroups()));
    }
}