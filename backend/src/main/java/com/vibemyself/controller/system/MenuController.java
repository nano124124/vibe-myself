package com.vibemyself.controller.system;

import com.vibemyself.common.response.ApiResponse;
import com.vibemyself.dto.system.MenuResponse;
import com.vibemyself.service.system.MenuService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/system/menus")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MenuResponse>>> getMenus() {
        return ResponseEntity.ok(ApiResponse.ok(menuService.getMenuTree()));
    }
}
