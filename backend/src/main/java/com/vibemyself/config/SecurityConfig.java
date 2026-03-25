package com.vibemyself.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vibemyself.common.jwt.JwtAuthFilter;
import com.vibemyself.common.jwt.JwtProvider;
import com.vibemyself.common.redis.RedisService;
import com.vibemyself.service.member.MemberAuthService;
import com.vibemyself.service.system.AdminAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final RedisService redisService;
    private final MemberAuthService memberAuthService;
    private final AdminAuthService adminAuthService;
    private final ObjectMapper objectMapper;

    @Bean
    public JwtAuthFilter jwtAuthFilter() {
        return new JwtAuthFilter(jwtProvider, redisService, memberAuthService, adminAuthService, objectMapper);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/api/member/login",
                    "/api/member/logout",
                    "/api/member/token/refresh",
                    "/api/admin/system/login",
                    "/api/admin/system/logout",
                    "/api/admin/system/token/refresh"
                ).permitAll()
                .requestMatchers("/api/admin/**").hasAnyRole("ADMIN", "SUPER")
                .requestMatchers("/api/member/**", "/api/cart/**", "/api/order/**").hasRole("USER")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
