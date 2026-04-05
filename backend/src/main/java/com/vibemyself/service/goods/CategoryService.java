package com.vibemyself.service.goods;

import com.vibemyself.common.util.SecurityUtils;
import com.vibemyself.dto.goods.CategoryResponse;
import com.vibemyself.dto.goods.CreateCategoryRequest;
import com.vibemyself.dto.goods.UpdateCategoryRequest;
import com.vibemyself.global.exception.AppException;
import com.vibemyself.global.exception.ErrorCode;
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
        String userId = SecurityUtils.currentUserId();
        PrCtgBase category = PrCtgBase.builder()
                .upCtgNo(request.getUpCtgNo())
                .ctgLvl(ctgLvl)
                .ctgNm(request.getCtgNm())
                .sortOrd(request.getSortOrd())
                .regId(userId)
                .modId(userId)
                .build();
        categoryMapper.insertCategory(category);
        return category.getCtgNo();
    }

    @Transactional
    public void updateCategory(Long ctgNo, UpdateCategoryRequest request) {
        if (categoryMapper.selectByCtgNo(ctgNo) == null) {
            throw new AppException(ErrorCode.CATEGORY_NOT_FOUND);
        }
        PrCtgBase category = PrCtgBase.builder()
                .ctgNo(ctgNo)
                .ctgNm(request.getCtgNm())
                .useYn(request.getUseYn())
                .sortOrd(request.getSortOrd())
                .modId(SecurityUtils.currentUserId())
                .build();
        categoryMapper.updateCategory(category);
    }

    private String resolveLevel(Long upCtgNo) {
        if (upCtgNo == null) {
            return "1";
        }
        PrCtgBase parent = categoryMapper.selectByCtgNo(upCtgNo);
        if (parent == null) {
            throw new AppException(ErrorCode.CATEGORY_PARENT_NOT_FOUND);
        }
        if ("3".equals(parent.getCtgLvl())) {
            throw new AppException(ErrorCode.CATEGORY_MAX_DEPTH_EXCEEDED);
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
