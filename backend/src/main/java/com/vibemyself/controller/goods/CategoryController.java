package com.vibemyself.controller.goods;

import com.vibemyself.common.response.ApiResponse;
import com.vibemyself.dto.goods.CategoryResponse;
import com.vibemyself.dto.goods.CreateCategoryRequest;
import com.vibemyself.dto.goods.CreateCategoryResponse;
import com.vibemyself.dto.goods.UpdateCategoryRequest;
import com.vibemyself.service.goods.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/goods/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.ok(categoryService.getCategoryTree()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CreateCategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        Long ctgNo = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(new CreateCategoryResponse(ctgNo)));
    }

    @PutMapping("/{ctgNo}")
    public ResponseEntity<ApiResponse<Void>> updateCategory(
            @PathVariable Long ctgNo,
            @Valid @RequestBody UpdateCategoryRequest request) {
        categoryService.updateCategory(ctgNo, request);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
