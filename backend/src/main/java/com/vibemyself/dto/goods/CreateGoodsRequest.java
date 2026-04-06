package com.vibemyself.dto.goods;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record CreateGoodsRequest(

        @NotBlank(message = "상품명은 필수입니다.")
        String goodsNm,

        @NotBlank(message = "상품유형 코드는 필수입니다.")
        String goodsTpCd,

        @NotNull(message = "카테고리는 필수입니다.")
        Long ctgNo,

        Long brandNo,

        @NotNull(message = "판매가는 필수입니다.")
        @Positive(message = "판매가는 0보다 커야 합니다.")
        BigDecimal salePrc,

        BigDecimal normPrc,

        BigDecimal suplyPrc,

        String goodsDesc,

        LocalDateTime saleStartDtm,

        LocalDateTime saleEndDtm,

        @NotBlank(message = "판매상태 코드는 필수입니다.")
        String saleStatCd,

        @NotBlank(message = "배송정책은 필수입니다.")
        String dlvPolicyNo,

        List<String> optGrpCds,

        @Valid
        List<UnitRequest> units
) {}
