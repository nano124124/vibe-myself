package com.vibemyself.common.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

public class WithMockAdminSecurityContextFactory
        implements WithSecurityContextFactory<WithMockAdmin> {

    @Override
    public SecurityContext createSecurityContext(WithMockAdmin annotation) {
        LoginUser loginUser = LoginUser.builder()
                .id(annotation.id())
                .loginId(annotation.loginId())
                .name(annotation.name())
                .type("admin")
                .role(annotation.role())
                .grade(null)
                .build();

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        return context;
    }
}
