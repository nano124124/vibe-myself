package com.vibemyself.model.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Menu {
    private Long menuNo;
    private Long parentMenuNo;
    private String menuNm;
    private String menuUrl;
    private int sortOrd;
}
