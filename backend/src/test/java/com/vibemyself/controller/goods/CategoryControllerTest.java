package com.vibemyself.controller.goods;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibemyself.dto.goods.CategoryResponse;
import com.vibemyself.dto.goods.CreateCategoryRequest;
import com.vibemyself.dto.goods.UpdateCategoryRequest;
import com.vibemyself.entity.PrCtgBase;
import com.vibemyself.global.exception.AppException;
import com.vibemyself.global.exception.ErrorCode;
import com.vibemyself.service.goods.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean CategoryService categoryService;

    @Test
    void getCategories_성공_200() throws Exception {
        PrCtgBase c = PrCtgBase.builder().ctgNo(1L).ctgNm("의류").ctgLvl("1").sortOrd(1).useYn("Y").build();
        given(categoryService.getCategoryTree()).willReturn(List.of(CategoryResponse.of(c, List.of())));

        mockMvc.perform(get("/api/admin/goods/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].ctgNm").value("의류"));
    }

    @Test
    void createCategory_성공_201() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(null, "의류", 1);
        given(categoryService.createCategory(any())).willReturn(1L);

        mockMvc.perform(post("/api/admin/goods/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.ctgNo").value(1));
    }

    @Test
    void createCategory_ctgNm빈값_400() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(null, "", 1);

        mockMvc.perform(post("/api/admin/goods/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createCategory_부모없음_400() throws Exception {
        CreateCategoryRequest request = new CreateCategoryRequest(999L, "하위", 1);
        given(categoryService.createCategory(any()))
                .willThrow(new AppException(ErrorCode.CATEGORY_PARENT_NOT_FOUND));

        mockMvc.perform(post("/api/admin/goods/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_성공_200() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("의류수정", "N", 2);

        mockMvc.perform(put("/api/admin/goods/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateCategory_ctgNm빈값_400() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("", "Y", 1);

        mockMvc.perform(put("/api/admin/goods/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateCategory_존재하지않음_404() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("없는카테", "Y", 1);
        willThrow(new AppException(ErrorCode.CATEGORY_NOT_FOUND)).given(categoryService).updateCategory(eq(999L), any());

        mockMvc.perform(put("/api/admin/goods/categories/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCategory_useYn잘못된값_400() throws Exception {
        UpdateCategoryRequest request = new UpdateCategoryRequest("의류", "X", 1);

        mockMvc.perform(put("/api/admin/goods/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
