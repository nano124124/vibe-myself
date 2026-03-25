package com.vibemyself.common.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements UserDetails {
    private String id;        // member: MBR_NO, admin: LOGIN_ID
    private String loginId;
    private String name;
    private String type;      // "member" | "admin"
    private String role;      // "ROLE_USER" | "ROLE_ADMIN" | "ROLE_SUPER"
    private String grade;     // 고객 등급코드 (admin은 null)

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role));
    }

    @Override public String getPassword() { return null; }
    @Override public String getUsername() { return loginId; }
}
