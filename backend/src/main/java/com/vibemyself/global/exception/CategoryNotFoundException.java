package com.vibemyself.global.exception;

public class CategoryNotFoundException extends BusinessException {
    public CategoryNotFoundException() {
        super("카테고리를 찾을 수 없습니다.", 404);
    }
}
