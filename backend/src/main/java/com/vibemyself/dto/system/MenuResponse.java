package com.vibemyself.dto.system;

import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class MenuResponse {
    private Long menuNo;
    private String menuNm;
    private String menuUrl;
    private int sortOrd;

    @Builder.Default
    private List<MenuResponse> children = new ArrayList<>();
}
