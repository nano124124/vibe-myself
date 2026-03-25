package com.vibemyself.common.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockMemberSecurityContextFactory.class)
public @interface WithMockMember {
    String id() default "M0001";
    String loginId() default "user@test.com";
    String name() default "테스터";
    String grade() default "BASIC";
}
