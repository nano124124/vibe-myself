package com.vibemyself.common.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockAdminSecurityContextFactory.class)
public @interface WithMockAdmin {
    String id() default "admin01";
    String loginId() default "admin01";
    String name() default "관리자1";
    String role() default "ROLE_ADMIN";
}
