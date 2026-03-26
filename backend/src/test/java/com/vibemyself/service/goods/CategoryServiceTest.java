package com.vibemyself.service.goods;

import com.vibemyself.dto.goods.CategoryResponse;
import com.vibemyself.dto.goods.CreateCategoryRequest;
import com.vibemyself.dto.goods.UpdateCategoryRequest;
import com.vibemyself.global.exception.AppException;
import com.vibemyself.global.exception.ErrorCode;
import com.vibemyself.mapper.goods.CategoryMapper;
import com.vibemyself.entity.PrCtgBase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock CategoryMapper categoryMapper;
    @InjectMocks CategoryService categoryService;

    // ── 조회 ─────────────────────────────────────────────

    @Test
    void getCategoryTree_빈목록_빈리스트반환() {
        given(categoryMapper.selectAll()).willReturn(List.of());
        List<CategoryResponse> result = categoryService.getCategoryTree();
        assertThat(result).isEmpty();
    }

    @Test
    void getCategoryTree_1단계만_루트1개반환() {
        PrCtgBase root = prCtgBase(1L, null, "1", "의류", 1);
        given(categoryMapper.selectAll()).willReturn(List.of(root));
        List<CategoryResponse> result = categoryService.getCategoryTree();
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCtgNm()).isEqualTo("의류");
        assertThat(result.get(0).getChildren()).isEmpty();
    }

    @Test
    void getCategoryTree_3단계트리_재귀조립() {
        PrCtgBase lv1 = prCtgBase(1L, null, "1", "의류", 1);
        PrCtgBase lv2 = prCtgBase(11L, 1L, "2", "상의", 1);
        PrCtgBase lv3 = prCtgBase(111L, 11L, "3", "반팔", 1);
        given(categoryMapper.selectAll()).willReturn(List.of(lv1, lv2, lv3));
        List<CategoryResponse> result = categoryService.getCategoryTree();
        assertThat(result).hasSize(1);
        CategoryResponse child = result.get(0).getChildren().get(0);
        assertThat(child.getCtgNm()).isEqualTo("상의");
        assertThat(child.getChildren().get(0).getCtgNm()).isEqualTo("반팔");
    }

    @Test
    void getCategoryTree_sortOrd기준정렬() {
        PrCtgBase a = prCtgBase(1L, null, "1", "신발", 2);
        PrCtgBase b = prCtgBase(2L, null, "1", "의류", 1);
        given(categoryMapper.selectAll()).willReturn(List.of(a, b));
        List<CategoryResponse> result = categoryService.getCategoryTree();
        assertThat(result.get(0).getCtgNm()).isEqualTo("의류");
        assertThat(result.get(1).getCtgNm()).isEqualTo("신발");
    }

    // ── 등록 ─────────────────────────────────────────────

    @Test
    void createCategory_최상위_CTG_LVL1로등록() {
        CreateCategoryRequest request = new CreateCategoryRequest(null, "의류", 1);
        categoryService.createCategory(request);
        then(categoryMapper).should().insertCategory(argThat(c ->
                c.getCtgLvl().equals("1") && c.getUpCtgNo() == null
        ));
    }

    @Test
    void createCategory_부모없음_400예외() {
        CreateCategoryRequest request = new CreateCategoryRequest(999L, "하위", 1);
        given(categoryMapper.selectByCtgNo(999L)).willReturn(null);
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_PARENT_NOT_FOUND);
    }

    @Test
    void createCategory_부모레벨3_400예외() {
        CreateCategoryRequest request = new CreateCategoryRequest(111L, "4단계", 1);
        given(categoryMapper.selectByCtgNo(111L))
                .willReturn(prCtgBase(111L, 11L, "3", "반팔", 1));
        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_MAX_DEPTH_EXCEEDED);
    }

    @Test
    void createCategory_최상위_생성된ctgNo반환() {
        CreateCategoryRequest request = new CreateCategoryRequest(null, "의류", 1);
        willAnswer(invocation -> {
            PrCtgBase c = invocation.getArgument(0);
            c.setCtgNo(1L);
            return null;
        }).given(categoryMapper).insertCategory(any());
        Long ctgNo = categoryService.createCategory(request);
        assertThat(ctgNo).isEqualTo(1L);
    }

    @Test
    void createCategory_2단계등록_CTG_LVL2로등록() {
        CreateCategoryRequest request = new CreateCategoryRequest(1L, "상의", 1);
        given(categoryMapper.selectByCtgNo(1L))
                .willReturn(prCtgBase(1L, null, "1", "의류", 1));
        categoryService.createCategory(request);
        then(categoryMapper).should().insertCategory(argThat(c ->
                c.getCtgLvl().equals("2") && c.getUpCtgNo().equals(1L)
        ));
    }

    // ── 수정 ─────────────────────────────────────────────

    @Test
    void updateCategory_존재하지않는카테고리_404예외() {
        given(categoryMapper.selectByCtgNo(999L)).willReturn(null);
        UpdateCategoryRequest request = new UpdateCategoryRequest("수정명", "Y", 1);
        assertThatThrownBy(() -> categoryService.updateCategory(999L, request))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.CATEGORY_NOT_FOUND);
    }

    @Test
    void updateCategory_정상수정_updateMapper호출() {
        given(categoryMapper.selectByCtgNo(1L))
                .willReturn(prCtgBase(1L, null, "1", "의류", 1));
        UpdateCategoryRequest request = new UpdateCategoryRequest("의류수정", "N", 2);
        categoryService.updateCategory(1L, request);
        then(categoryMapper).should().updateCategory(argThat(c ->
                c.getCtgNo().equals(1L)
                        && c.getCtgNm().equals("의류수정")
                        && c.getUseYn().equals("N")
                        && c.getSortOrd() == 2
        ));
    }

    // ── 헬퍼 ─────────────────────────────────────────────

    private PrCtgBase prCtgBase(Long ctgNo, Long upCtgNo, String ctgLvl, String ctgNm, int sortOrd) {
        return PrCtgBase.builder()
                .ctgNo(ctgNo).upCtgNo(upCtgNo).ctgLvl(ctgLvl)
                .ctgNm(ctgNm).sortOrd(sortOrd).useYn("Y").build();
    }
}
