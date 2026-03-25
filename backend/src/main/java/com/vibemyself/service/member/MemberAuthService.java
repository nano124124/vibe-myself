package com.vibemyself.service.member;

import com.vibemyself.common.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberAuthService {

    public LoginUser loadUser(String id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
