package com.vibemyself.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── 인증/인가 ──────────────────────────────────────────
    UNAUTHORIZED("인증이 필요합니다.", 401),
    INVALID_CREDENTIALS("아이디 또는 비밀번호가 올바르지 않습니다.", 401),
    ACCOUNT_DISABLED("사용할 수 없는 계정입니다.", 401),
    INVALID_REFRESH_TOKEN("유효하지 않은 refresh token입니다.", 401),

    // ── 회원 ──────────────────────────────────────────────
    MEMBER_NOT_FOUND("회원을 찾을 수 없습니다.", 404),
    MEMBER_ALREADY_EXISTS("이미 존재하는 회원입니다.", 409),

    // ── 카테고리 ──────────────────────────────────────────
    CATEGORY_NOT_FOUND("카테고리를 찾을 수 없습니다.", 404),
    CATEGORY_PARENT_NOT_FOUND("상위 카테고리가 존재하지 않습니다.", 400),
    CATEGORY_MAX_DEPTH_EXCEEDED("3단계 이하로는 카테고리를 등록할 수 없습니다.", 400),

    // ── 상품 ──────────────────────────────────────────────
    BRAND_NOT_FOUND("브랜드를 찾을 수 없습니다.", 404),
    DLV_POLICY_NOT_FOUND("배송정책을 찾을 수 없습니다.", 404),
    OPT_GRP_NOT_FOUND("옵션 그룹을 찾을 수 없습니다.", 404),
    OPT_ITM_NOT_FOUND("옵션 항목을 찾을 수 없습니다.", 404),
    GOODS_IMG_LIMIT_EXCEEDED("상품 이미지는 최대 5장까지 등록할 수 있습니다.", 400);

    private final String message;
    private final int status;
}
