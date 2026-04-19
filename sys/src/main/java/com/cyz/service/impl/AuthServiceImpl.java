package com.cyz.service.impl;

import com.cyz.common.constant.CacheConstant;
import com.cyz.common.exception.BizException;
import com.cyz.common.enums.LoginType;
import com.cyz.common.enums.UserStatus;
import com.cyz.common.util.IpUtil;
import com.cyz.common.util.Md5Util;
import com.cyz.dto.request.LoginRequest;
import com.cyz.dto.response.LoginResponse;
import com.cyz.dto.response.LoginUserResponse;
import com.cyz.entity.SysLoginLog;
import com.cyz.entity.SysMenu;
import com.cyz.entity.SysRole;
import com.cyz.entity.SysRoleMenu;
import com.cyz.entity.SysUser;
import com.cyz.entity.SysUserRole;
import com.cyz.repository.SysConfigRepository;
import com.cyz.repository.SysMenuRepository;
import com.cyz.repository.SysRoleMenuRepository;
import com.cyz.repository.SysRoleRepository;
import com.cyz.repository.SysUserRepository;
import com.cyz.repository.SysUserRoleRepository;
import com.cyz.security.JwtTokenProvider;
import com.cyz.security.LoginUser;
import com.cyz.security.LoginUserContext;
import com.cyz.service.AuthService;
import com.cyz.service.DictService;
import com.cyz.service.LoginLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserRepository userRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysRoleRepository roleRepository;
    private final SysRoleMenuRepository roleMenuRepository;
    private final SysMenuRepository menuRepository;
    private final SysConfigRepository configRepository;
    private final DictService dictService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;
    private final LoginLogService loginLogService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String clientIp = IpUtil.getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        SysUser user = userRepository.findByUsernameAndIsDeletedFalse(request.getUsername())
                .orElse(null);

        if (user == null) {
            recordLoginLog(null, request.getUsername(), LoginType.LOGIN.name(), "fail", "用户名或密码错误", clientIp, userAgent, false);
            throw new BizException(401, "用户名或密码错误");
        }

        if (!dictService.getDictValue(UserStatus.class.getSimpleName(), UserStatus.NORMAL.name()).equals(user.getStatus())) {
            recordLoginLog(user, user.getUsername(), LoginType.LOGIN.name(), "fail", "账号已被锁定或禁用", clientIp, userAgent, false);
            throw new BizException(401, "账号已被锁定或禁用");
        }

        int maxFailCount = getConfigInt("login_fail_max_count", 5);
        if (user.getLoginFailCount() != null && user.getLoginFailCount() >= maxFailCount) {
            user.setStatus(dictService.getDictValue(UserStatus.class.getSimpleName(), UserStatus.LOCKED.name()));
            userRepository.save(user);
            recordLoginLog(user, user.getUsername(), LoginType.LOGIN.name(), "fail", "登录失败次数过多，账号已锁定", clientIp, userAgent, true);
            throw new BizException(401, "登录失败次数过多，账号已锁定");
        }

        if (!Md5Util.encrypt(request.getPassword()).equals(user.getPassword())) {
            int failCount = (user.getLoginFailCount() != null ? user.getLoginFailCount() : 0) + 1;
            user.setLoginFailCount(failCount);
            if (failCount >= maxFailCount) {
                user.setStatus(dictService.getDictValue(UserStatus.class.getSimpleName(), UserStatus.LOCKED.name()));
            }
            userRepository.save(user);
            recordLoginLog(user, user.getUsername(), LoginType.LOGIN.name(), "fail", "密码错误", clientIp, userAgent, false);
            throw new BizException(401, "用户名或密码错误");
        }

        // 密码过期检查
        int expireDays = getConfigInt("password_expire_days", 0);
        if (expireDays > 0 && user.getPasswordUpdateTime() != null) {
            long daysSinceChange = ChronoUnit.DAYS.between(user.getPasswordUpdateTime(), LocalDateTime.now());
            if (daysSinceChange > expireDays) {
                recordLoginLog(user, user.getUsername(), LoginType.LOGIN.name(), "fail", "密码已过期，请联系管理员重置", clientIp, userAgent, true);
                throw new BizException(401, "密码已过期，请联系管理员重置密码");
            }
        }

        // 生成 JWT
        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        LoginUser loginUser = LoginUser.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .orgCode(user.getOrgCode())
                .token(token)
                .clientIp(clientIp)
                .loginTime(System.currentTimeMillis())
                .build();

        long expirationMs = jwtTokenProvider.getExpirationMs();
        String onlineKey = CacheConstant.ONLINE_USER_PREFIX + user.getId();
        redisTemplate.opsForValue().set(onlineKey, loginUser, expirationMs, TimeUnit.MILLISECONDS);

        user.setLastLoginTime(LocalDateTime.now());
        user.setLoginFailCount(0);
        userRepository.save(user);

        recordLoginLog(user, user.getUsername(), LoginType.LOGIN.name(), "success", null, clientIp, userAgent, false);

        LoginUserResponse userInfo = buildLoginUserResponse(user);

        return LoginResponse.builder()
                .token(token)
                .userInfo(userInfo)
                .build();
    }

    @Override
    public void logout() {
        LoginUser loginUser = LoginUserContext.get();
        if (loginUser != null) {
            String onlineKey = CacheConstant.ONLINE_USER_PREFIX + loginUser.getUserId();
            redisTemplate.delete(onlineKey);

            SysLoginLog loginLog = new SysLoginLog();
            loginLog.setUserId(loginUser.getUserId());
            loginLog.setUsername(loginUser.getUsername());
            loginLog.setLoginIp(loginUser.getClientIp());
            loginLog.setLoginType(LoginType.LOGOUT.name());
            loginLog.setLoginResult("success");
            loginLog.setLoginTime(LocalDateTime.now());
            loginLog.setLogoutTime(LocalDateTime.now());
            loginLogService.save(loginLog);
        }
    }

    @Override
    public LoginUserResponse getCurrentUser() {
        LoginUser loginUser = LoginUserContext.get();
        if (loginUser == null) {
            throw new BizException(401, "未登录");
        }

        SysUser user = userRepository.findByIdAndIsDeletedFalse(loginUser.getUserId())
                .orElseThrow(() -> new BizException(401, "用户不存在"));

        return buildLoginUserResponse(user);
    }

    private void recordLoginLog(SysUser user, String username, String loginType,
                                String loginResult, String failReason, String clientIp,
                                String userAgent, boolean isAbnormal) {
        SysLoginLog loginLog = new SysLoginLog();
        if (user != null) {
            loginLog.setUserId(user.getId());
        }
        loginLog.setUsername(username);
        loginLog.setLoginIp(clientIp);
        loginLog.setLoginDevice(userAgent);
        loginLog.setLoginType(loginType);
        loginLog.setLoginResult(loginResult);
        loginLog.setFailReason(failReason);
        loginLog.setLoginTime(LocalDateTime.now());
        loginLog.setIsAbnormal(isAbnormal);
        loginLogService.save(loginLog);
    }

    private LoginUserResponse buildLoginUserResponse(SysUser user) {
        List<SysUserRole> userRoles = userRoleRepository.findByUserIdAndIsDeletedFalse(user.getId());
        List<Long> roleIds = userRoles.stream().map(SysUserRole::getRoleId).collect(Collectors.toList());

        if (roleIds.isEmpty()) {
            return LoginUserResponse.builder()
                    .userId(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .orgCode(user.getOrgCode())
                    .roles(new ArrayList<>())
                    .perms(new ArrayList<>())
                    .build();
        }

        List<SysRole> roles = roleRepository.findByIdInAndIsDeletedFalse(roleIds);
        List<String> roleCodes = roles.stream().map(SysRole::getRoleCode).collect(Collectors.toList());

        List<SysRoleMenu> roleMenus = roleMenuRepository.findByRoleIdInAndIsDeletedFalse(roleIds);
        Set<Long> menuIds = roleMenus.stream().map(SysRoleMenu::getMenuId).collect(Collectors.toSet());

        Set<String> permsSet = new HashSet<>();
        if (!menuIds.isEmpty()) {
            List<SysMenu> menus = menuRepository.findByIdInAndIsDeletedFalse(menuIds);
            for (SysMenu menu : menus) {
                if (menu.getPerms() != null && !menu.getPerms().isEmpty()) {
                    permsSet.add(menu.getPerms());
                }
            }
        }

        return LoginUserResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .orgCode(user.getOrgCode())
                .roles(roleCodes)
                .perms(new ArrayList<>(permsSet))
                .build();
    }

    private int getConfigInt(String key, int defaultValue) {
        return configRepository.findByConfigKeyAndIsDeletedFalse(key)
                .map(config -> {
                    try {
                        return Integer.parseInt(config.getConfigValue());
                    } catch (NumberFormatException e) {
                        return defaultValue;
                    }
                })
                .orElse(defaultValue);
    }

}
