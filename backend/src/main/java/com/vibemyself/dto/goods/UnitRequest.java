package com.vibemyself.dto.goods;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

import java.math.BigDecimal;
import java.util.List;

public record UnitRequest(

        @NotEmpty(message = "단품 옵션 항목은 필수입니다.")
        @Valid
        List<UnitOptRequest> optItms,

        @Min(value = 0, message = "추가 가격은 0 이상이어야 합니다.")
        BigDecimal addPrc,

        @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다.")
        int stockQty
) {}
