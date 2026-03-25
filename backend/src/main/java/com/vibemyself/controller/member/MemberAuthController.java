package com.vibemyself.controller.member;

import com.vibemyself.common.response.ApiResponse;
import com.vibemyself.dto.member.LoginMemberRequest;
import com.vibemyself.service.member.MemberAuthService;
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
@RequestMapping("/api/member")
@RequiredArgsConstructor
public class MemberAuthController {

    private final MemberAuthService memberAuthService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Void>> login(
            @RequestBody @Valid LoginMemberRequest request,
            HttpServletResponse response) {
        memberAuthService.login(request, response);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request, HttpServletResponse response) {
        memberAuthService.logout(request, response);
        return ResponseEntity.ok(ApiResponse.ok());
    }

    @PostMapping("/token/refresh")
    public ResponseEntity<ApiResponse<Void>> refresh(
            HttpServletRequest request, HttpServletResponse response) {
        memberAuthService.refresh(request, response);
        return ResponseEntity.ok(ApiResponse.ok());
    }
}
