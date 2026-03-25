package com.vibemyself.controller.system;

import com.vibemyself.common.response.ApiResponse;
import com.vibemyself.dto.system.LoginAdminRequest;
import com.vibemyself.service.system.AdminAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/system")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @RequestBody @Valid LoginAdminRequest request,
            HttpServletResponse response) {
        adminAuthService.login(request, response);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request, HttpServletResponse response) {
        adminAuthService.logout(request, response);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            HttpServletRequest request, HttpServletResponse response) {
        adminAuthService.refresh(request, response);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
