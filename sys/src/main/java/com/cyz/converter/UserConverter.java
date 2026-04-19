package com.cyz.converter;

import com.cyz.common.util.Md5Util;
import com.cyz.dto.request.UserCreateRequest;
import com.cyz.dto.request.UserUpdateRequest;
import com.cyz.dto.response.UserResponse;
import com.cyz.entity.SysUser;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class UserConverter {

    public SysUser toEntity(UserCreateRequest request) {
        return SysUser.builder()
                .username(request.getUsername())
                .password(Md5Util.encrypt(request.getPassword()))
                .nickname(request.getNickname())
                .personId(request.getPersonId())
                .orgCode(request.getOrgCode())
                .status(request.getStatus() != null ? request.getStatus() : "1")
                .loginFailCount(0)
                .passwordUpdateTime(LocalDateTime.now())
                .build();
    }

    public void updateEntity(SysUser entity, UserUpdateRequest request) {
        if (request.getNickname() != null) {
            entity.setNickname(request.getNickname());
        }
        if (request.getPersonId() != null) {
            entity.setPersonId(request.getPersonId());
        }
        if (request.getOrgCode() != null) {
            entity.setOrgCode(request.getOrgCode());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
    }

    public UserResponse toResponse(SysUser entity) {
        return toResponse(entity, null);
    }

    public UserResponse toResponse(SysUser entity, String orgName) {
        UserResponse response = new UserResponse();
        response.setId(entity.getId());
        response.setUsername(entity.getUsername());
        response.setNickname(entity.getNickname());
        response.setPersonId(entity.getPersonId());
        response.setOrgCode(entity.getOrgCode());
        response.setOrgName(orgName);
        response.setStatus(entity.getStatus());
        response.setLoginFailCount(entity.getLoginFailCount());
        response.setLastLoginTime(entity.getLastLoginTime());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());
        return response;
    }

    public UserResponse toResponse(SysUser entity, List<String> roleNames, boolean online) {
        return toResponse(entity, null, roleNames, online);
    }

    public UserResponse toResponse(SysUser entity, String orgName, List<String> roleNames, boolean online) {
        UserResponse response = toResponse(entity, orgName);
        response.setRoleNames(roleNames);
        response.setOnline(online);
        return response;
    }

    private static final String LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*_-+=";
    private static final String ALL = LOWER + UPPER + DIGITS + SPECIAL;
    private static final SecureRandom RANDOM = new SecureRandom();

    /**
     * @param level 密码强度等级：1-低 2-中 3-高
     */
    public static void validatePasswordStrength(String password, int level) {
        if (password == null || password.isEmpty()) {
            return;
        }
        switch (level) {
            case 3 -> {
                if (password.length() < 10
                        || !password.matches(".*[a-z].*")
                        || !password.matches(".*[A-Z].*")
                        || !password.matches(".*\\d.*")
                        || !password.matches(".*[^a-zA-Z0-9].*")) {
                    throw new com.cyz.common.exception.BizException(
                            "密码强度不足：需至少10位，包含大小写字母、数字和特殊字符");
                }
            }
            case 2 -> {
                if (password.length() < 8
                        || !password.matches(".*[a-zA-Z].*")
                        || !password.matches(".*\\d.*")) {
                    throw new com.cyz.common.exception.BizException(
                            "密码强度不足：需至少8位，包含字母和数字");
                }
            }
            default -> {
                if (password.length() < 6) {
                    throw new com.cyz.common.exception.BizException("密码长度不能少于6位");
                }
            }
        }
    }

    /**
     * 根据密码强度等级生成随机密码
     *
     * @param level 密码强度等级：1-低(>=6位) 2-中(>=8位含字母数字) 3-高(>=10位含大小写数字特殊字符)
     * @return 符合强度要求的随机密码
     */
    public static String generateRandomPassword(int level) {
        return switch (level) {
            case 3 -> generateStrongPassword();
            case 2 -> generateMediumPassword();
            default -> generateSimplePassword();
        };
    }

    private static String generateSimplePassword() {
        int length = 6 + RANDOM.nextInt(4); // 6-9位
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }
        return sb.toString();
    }

    private static String generateMediumPassword() {
        int length = 8 + RANDOM.nextInt(4); // 8-11位
        StringBuilder sb = new StringBuilder(length);
        // 确保至少包含一个字母和一个数字
        sb.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        sb.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        // 填充剩余字符
        String pool = LOWER + UPPER + DIGITS;
        for (int i = 2; i < length; i++) {
            sb.append(pool.charAt(RANDOM.nextInt(pool.length())));
        }
        // 打乱顺序
        return shuffle(sb.toString());
    }

    private static String generateStrongPassword() {
        int length = 10 + RANDOM.nextInt(4); // 10-13位
        StringBuilder sb = new StringBuilder(length);
        // 确保至少包含各类字符各一个
        sb.append(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        sb.append(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        sb.append(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        sb.append(SPECIAL.charAt(RANDOM.nextInt(SPECIAL.length())));
        // 填充剩余字符
        for (int i = 4; i < length; i++) {
            sb.append(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }
        // 打乱顺序
        return shuffle(sb.toString());
    }

    private static String shuffle(String input) {
        char[] chars = input.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = RANDOM.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}
