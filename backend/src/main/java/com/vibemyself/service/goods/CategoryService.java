package com.vibemyself.service.goods;

import com.vibemyself.dto.goods.CategoryResponse;
import com.vibemyself.dto.goods.CreateCategoryRequest;
import com.vibemyself.dto.goods.UpdateCategoryRequest;
import com.vibemyself.global.exception.CategoryNotFoundException;
import com.vibemyself.mapper.goods.CategoryMapper;
import com.vibemyself.entity.PrCtgBase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryTree() {
        List<PrCtgBase> all = categoryMapper.selectAll();
        return buildTree(all, null);
    }

    @Transactional
    public Long createCategory(CreateCategoryRequest request) {
        String ctgLvl = resolveLevel(request.getUpCtgNo());
        PrCtgBase category = PrCtgBase.builder()
                .upCtgNo(request.getUpCtgNo())
                .ctgLvl(ctgLvl)
                .ctgNm(request.getCtgNm())
                .sortOrd(request.getSortOrd())
                .build();
        categoryMapper.insertCategory(category);
        return category.getCtgNo();
    }

    @Transactional
    public void updateCategory(Long ctgNo, UpdateCategoryRequest request) {
        if (categoryMapper.selectByCtgNo(ctgNo) == null) {
            throw new CategoryNotFoundException();
        }
        PrCtgBase category = PrCtgBase.builder()
                .ctgNo(ctgNo)
                .ctgNm(request.getCtgNm())
                .useYn(request.getUseYn())
                .sortOrd(request.getSortOrd())
                .build();
        categoryMapper.updateCategory(category);
    }

    private String resolveLevel(Long upCtgNo) {
        if (upCtgNo == null) {
            return "1";
        }
        PrCtgBase parent = categoryMapper.selectByCtgNo(upCtgNo);
        if (parent == null) {
            throw new IllegalArgumentException("상위 카테고리가 존재하지 않습니다.");
        }
        if ("3".equals(parent.getCtgLvl())) {
            throw new IllegalArgumentException("3단계 이하로는 카테고리를 등록할 수 없습니다.");
        }
        return String.valueOf(Integer.parseInt(parent.getCtgLvl()) + 1);
    }

    private List<CategoryResponse> buildTree(List<PrCtgBase> all, Long upCtgNo) {
        return all.stream()
                .filter(c -> Objects.equals(c.getUpCtgNo(), upCtgNo))
                .sorted(Comparator.comparingInt(PrCtgBase::getSortOrd))
                .map(c -> CategoryResponse.of(c, buildTree(all, c.getCtgNo())))
                .toList();
    }
}
