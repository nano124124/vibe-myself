package com.vibemyself.service.system;

import com.vibemyself.dto.system.MenuResponse;
import com.vibemyself.mapper.system.MenuMapper;
import com.vibemyself.model.system.Menu;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuMapper menuMapper;

    @InjectMocks
    private MenuService menuService;

    @Test
    void 대메뉴와_소메뉴가_트리로_조립된다() {
        // given
        List<Menu> flatMenus = List.of(
                Menu.builder().menuNo(1L).parentMenuNo(null).menuNm("대시보드").menuUrl("/admin/dashboard").sortOrd(1).build(),
                Menu.builder().menuNo(2L).parentMenuNo(null).menuNm("상품관리").menuUrl(null).sortOrd(2).build(),
                Menu.builder().menuNo(11L).parentMenuNo(2L).menuNm("상품목록").menuUrl("/admin/products").sortOrd(1).build()
        );
        when(menuMapper.selectAllActive()).thenReturn(flatMenus);

        // when
        List<MenuResponse> result = menuService.getMenuTree();

        // then
        assertThat(result).hasSize(2);

        MenuResponse dashboard = result.get(0);
        assertThat(dashboard.getMenuNo()).isEqualTo(1L);
        assertThat(dashboard.getMenuNm()).isEqualTo("대시보드");
        assertThat(dashboard.getChildren()).isEmpty();

        MenuResponse goods = result.get(1);
        assertThat(goods.getMenuNo()).isEqualTo(2L);
        assertThat(goods.getChildren()).hasSize(1);
        assertThat(goods.getChildren().get(0).getMenuNm()).isEqualTo("상품목록");
    }

    @Test
    void 루트_메뉴는_sortOrd_기준으로_정렬된다() {
        // given — sortOrd가 3, 1, 2 순서로 섞여 있어도 1, 2, 3 순으로 정렬되어야 함
        List<Menu> flatMenus = List.of(
                Menu.builder().menuNo(3L).parentMenuNo(null).menuNm("주문관리").menuUrl(null).sortOrd(3).build(),
                Menu.builder().menuNo(1L).parentMenuNo(null).menuNm("대시보드").menuUrl("/admin/dashboard").sortOrd(1).build(),
                Menu.builder().menuNo(2L).parentMenuNo(null).menuNm("상품관리").menuUrl(null).sortOrd(2).build()
        );
        when(menuMapper.selectAllActive()).thenReturn(flatMenus);

        // when
        List<MenuResponse> result = menuService.getMenuTree();

        // then
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getMenuNm()).isEqualTo("대시보드");
        assertThat(result.get(1).getMenuNm()).isEqualTo("상품관리");
        assertThat(result.get(2).getMenuNm()).isEqualTo("주문관리");
    }

    @Test
    void 소메뉴는_부모의_children에_sortOrd_기준으로_정렬된다() {
        // given
        List<Menu> flatMenus = List.of(
                Menu.builder().menuNo(3L).parentMenuNo(null).menuNm("주문관리").menuUrl(null).sortOrd(3).build(),
                Menu.builder().menuNo(13L).parentMenuNo(3L).menuNm("클레임목록").menuUrl("/admin/claims").sortOrd(2).build(),
                Menu.builder().menuNo(12L).parentMenuNo(3L).menuNm("주문목록").menuUrl("/admin/orders").sortOrd(1).build()
        );
        when(menuMapper.selectAllActive()).thenReturn(flatMenus);

        // when
        List<MenuResponse> result = menuService.getMenuTree();

        // then
        List<MenuResponse> children = result.get(0).getChildren();
        assertThat(children).hasSize(2);
        assertThat(children.get(0).getMenuNm()).isEqualTo("주문목록");
        assertThat(children.get(1).getMenuNm()).isEqualTo("클레임목록");
    }
}
