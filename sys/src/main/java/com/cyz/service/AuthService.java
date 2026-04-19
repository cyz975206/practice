package com.cyz.service;

import com.cyz.dto.request.LoginRequest;
import com.cyz.dto.response.LoginResponse;
import com.cyz.dto.response.LoginUserResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {

    LoginResponse login(LoginRequest request, HttpServletRequest httpRequest);

    void logout();

    LoginUserResponse getCurrentUser();
}
