package com.vibemyself.dto.goods;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {
    private Long upCtgNo;          // null이면 최상위

    @NotBlank(message = "카테고리명은 필수입니다.")
    private String ctgNm;

    private int sortOrd;
}
