package com.vibemyself.dto.goods;

import jakarta.validation.constraints.NotBlank;

public record UnitOptRequest(

        @NotBlank(message = "옵션 그룹 코드는 필수입니다.")
        String optGrpCd,

        @NotBlank(message = "옵션 항목 코드는 필수입니다.")
        String optItmCd
) {}
