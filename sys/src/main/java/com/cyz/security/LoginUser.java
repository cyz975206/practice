package com.cyz.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginUser implements Serializable {

    private Long userId;
    private String username;
    private String nickname;
    private String orgCode;
    private String token;
    private String clientIp;
    private long loginTime;
}
