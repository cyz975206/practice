package com.cyz.seal.iam.application;

import com.cyz.seal.iam.interfaces.dto.LoginRequest;
import com.cyz.seal.iam.interfaces.dto.LoginResponse;

/** 认证服务。 */
public interface AuthService {

    /** 登录：校验密码 → 签发 JWT。 */
    LoginResponse login(LoginRequest request);
}
