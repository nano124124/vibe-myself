package com.vibemyself.controller.goods;

import com.vibemyself.common.response.ApiResponse;
import com.vibemyself.dto.goods.DlvPolicyResponse;
import com.vibemyself.service.goods.DlvPolicyQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/goods/dlv-policies")
@RequiredArgsConstructor
public class DlvPolicyController {

    private final DlvPolicyQueryService dlvPolicyQueryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<DlvPolicyResponse>>> getDlvPolicies() {
        return ResponseEntity.ok(ApiResponse.ok(dlvPolicyQueryService.getDlvPolicies()));
    }
}