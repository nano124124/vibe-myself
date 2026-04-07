package com.vibemyself.service.goods;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class GoodsConstants {

    private GoodsConstants() {}

    public static final String PRC_APLY_TO_DT_INFINITY = "99991231";
    public static final DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    public static final LocalDateTime SALE_END_DTM_DEFAULT = LocalDateTime.of(2999, 12, 31, 0, 0, 0);
}
