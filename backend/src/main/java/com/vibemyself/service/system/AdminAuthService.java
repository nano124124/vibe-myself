package com.vibemyself.service.system;

import com.vibemyself.common.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    public LoginUser loadUser(String id) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
