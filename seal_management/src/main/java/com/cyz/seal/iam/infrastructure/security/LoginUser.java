package com.cyz.seal.iam.infrastructure.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 登录态：由 JWT claim 重建（每请求无需查库）。
 */
public class LoginUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final Long legalEntityId;
    private final List<String> roleCodes;

    public LoginUser(Long userId, String username, Long legalEntityId, List<String> roleCodes) {
        this.userId = userId;
        this.username = username;
        this.legalEntityId = legalEntityId;
        this.roleCodes = roleCodes == null ? List.of() : roleCodes;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getLegalEntityId() {
        return legalEntityId;
    }

    public List<String> getRoleCodes() {
        return roleCodes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roleCodes.stream()
                .map(code -> (GrantedAuthority) () -> "ROLE_" + code)
                .toList();
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
