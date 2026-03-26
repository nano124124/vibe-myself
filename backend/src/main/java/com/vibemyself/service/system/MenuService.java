package com.vibemyself.service.system;

import com.vibemyself.dto.system.MenuResponse;
import com.vibemyself.entity.StMenuBase;
import com.vibemyself.mapper.system.MenuMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final MenuMapper menuMapper;

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenuTree() {
        List<StMenuBase> flatMenus = menuMapper.selectAllActive();

        Map<Long, MenuResponse> index = new LinkedHashMap<>();
        for (StMenuBase menu : flatMenus) {
            index.put(menu.getMenuNo(), MenuResponse.builder()
                    .menuNo(menu.getMenuNo())
                    .menuNm(menu.getMenuNm())
                    .menuUrl(menu.getMenuUrl())
                    .sortOrd(menu.getSortOrd())
                    .build());
        }

        List<MenuResponse> roots = new ArrayList<>();
        for (StMenuBase menu : flatMenus) {
            MenuResponse response = index.get(menu.getMenuNo());
            if (menu.getParentMenuNo() == null) {
                roots.add(response);
            } else {
                MenuResponse parent = index.get(menu.getParentMenuNo());
                if (parent != null) {
                    parent.getChildren().add(response);
                }
            }
        }

        roots.sort(Comparator.comparingInt(MenuResponse::getSortOrd));
        // 현재 2단계까지만 정렬. 3단계 이상 추가 시 재귀 정렬로 전환 필요
        roots.forEach(root -> root.getChildren().sort(Comparator.comparingInt(MenuResponse::getSortOrd)));

        return roots;
    }
}
