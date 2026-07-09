package com.cyz.seal.iam.interfaces.dto;

import java.util.List;

/** 登录响应。 */
public record LoginResponse(
        String token,
        UserInfo user
) {
    public record UserInfo(
            Long userId,
            String username,
            String nickname,
            Long legalEntityId,
            List<String> roleCodes
    ) {
    }
}
