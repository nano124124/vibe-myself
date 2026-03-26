package com.vibemyself.dto.goods;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCategoryRequest {
    @NotBlank(message = "카테고리명은 필수입니다.")
    private String ctgNm;

    @NotNull(message = "사용여부는 필수입니다.")
    @Pattern(regexp = "[YN]", message = "사용여부는 Y 또는 N이어야 합니다.")
    private String useYn;

    @NotNull(message = "정렬순서는 필수입니다.")
    private Integer sortOrd;
}
