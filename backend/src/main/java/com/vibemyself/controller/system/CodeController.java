package com.vibemyself.controller.system;

import com.vibemyself.dto.system.CodeResponse;
import com.vibemyself.global.ApiResponse;
import com.vibemyself.service.system.CodeQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
public class CodeController {

    private final CodeQueryService codeQueryService;

    @GetMapping("/codes/{codeGrpCd}")
    public ApiResponse<List<CodeResponse>> getCodes(@PathVariable String codeGrpCd) {
        return ApiResponse.success(codeQueryService.getCodes(codeGrpCd));
    }
}
