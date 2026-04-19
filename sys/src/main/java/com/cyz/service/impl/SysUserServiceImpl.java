package com.cyz.service.impl;

import com.cyz.common.constant.CacheConstant;
import com.cyz.common.enums.CommonStatus;
import com.cyz.common.enums.UserStatus;
import com.cyz.common.exception.BizException;
import com.cyz.common.util.Md5Util;
import com.cyz.converter.UserConverter;
import com.cyz.dto.request.UserAssignRolesRequest;
import com.cyz.dto.request.UserChangePasswordRequest;
import com.cyz.dto.request.UserCreateRequest;
import com.cyz.dto.request.UserQueryRequest;
import com.cyz.dto.request.UserResetPasswordRequest;
import com.cyz.dto.request.UserUpdateRequest;
import com.cyz.dto.response.UserResponse;
import com.cyz.entity.SysOrg;
import com.cyz.entity.SysRole;
import com.cyz.entity.SysUser;
import com.cyz.entity.SysUserRole;
import com.cyz.repository.SysConfigRepository;
import com.cyz.repository.SysOrgRepository;
import com.cyz.repository.SysPersonRepository;
import com.cyz.repository.SysRoleRepository;
import com.cyz.repository.SysUserRepository;
import com.cyz.repository.SysUserRoleRepository;
import com.cyz.security.LoginUserContext;
import com.cyz.service.DictService;
import com.cyz.service.SysUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysUserServiceImpl implements SysUserService {

    private final SysUserRepository userRepository;
    private final SysPersonRepository personRepository;
    private final SysOrgRepository orgRepository;
    private final SysRoleRepository roleRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final SysConfigRepository configRepository;
    private final DictService dictService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserConverter userConverter;

    @Override
    @Transactional
    public UserResponse create(UserCreateRequest request) {
        orgRepository.findByOrgCodeAndIsDeletedFalse(request.getOrgCode())
                .orElseThrow(() -> new BizException("所属机构不存在: " + request.getOrgCode()));

        if (userRepository.existsByUsernameAndIsDeletedFalse(request.getUsername())) {
            throw new BizException("用户名已存在: " + request.getUsername());
        }

        if (request.getPersonId() != null) {
            personRepository.findByIdAndIsDeletedFalse(request.getPersonId())
                    .orElseThrow(() -> new BizException("关联人员不存在: " + request.getPersonId()));
        }

        int strengthLevel = getConfigInt("password_strength_level", 1);
        UserConverter.validatePasswordStrength(request.getPassword(), strengthLevel);

        SysUser user = userConverter.toEntity(request);
        SysUser saved = userRepository.save(user);
        String orgName = orgRepository.findByOrgCodeAndIsDeletedFalse(request.getOrgCode())
                .map(SysOrg::getOrgShortName).orElse(null);
        return userConverter.toResponse(saved, orgName);
    }

    @Override
    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        SysUser user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("用户不存在: " + id));

        if (request.getOrgCode() != null) {
            orgRepository.findByOrgCodeAndIsDeletedFalse(request.getOrgCode())
                    .orElseThrow(() -> new BizException("所属机构不存在: " + request.getOrgCode()));
        }

        if (request.getPersonId() != null) {
            personRepository.findByIdAndIsDeletedFalse(request.getPersonId())
                    .orElseThrow(() -> new BizException("关联人员不存在: " + request.getPersonId()));
        }

        userConverter.updateEntity(user, request);
        SysUser saved = userRepository.save(user);
        String orgCode = user.getOrgCode();
        String orgName = orgCode != null ? orgRepository.findByOrgCodeAndIsDeletedFalse(orgCode)
                .map(SysOrg::getOrgShortName).orElse(null) : null;
        return userConverter.toResponse(saved, orgName);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysUser user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("用户不存在: " + id));

        user.setIsDeleted(true);
        userRepository.save(user);

        List<SysUserRole> userRoles = userRoleRepository.findByUserIdAndIsDeletedFalse(id);
        userRoles.forEach(ur -> ur.setIsDeleted(true));
        if (!userRoles.isEmpty()) {
            userRoleRepository.saveAll(userRoles);
        }
    }

    @Override
    public UserResponse getById(Long id) {
        SysUser user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("用户不存在: " + id));
        String orgName = user.getOrgCode() != null ? orgRepository.findByOrgCodeAndIsDeletedFalse(user.getOrgCode())
                .map(SysOrg::getOrgShortName).orElse(null) : null;
        return userConverter.toResponse(user, orgName);
    }

    @Override
    public Page<UserResponse> list(UserQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<SysUser> page = userRepository.findByConditions(
                request.getUsername(), request.getNickname(),
                request.getOrgCode(), request.getStatus(), pageRequest);

        List<SysUser> users = page.getContent();
        if (users.isEmpty()) {
            return new PageImpl<>(List.of(), pageRequest, page.getTotalElements());
        }

        // 批量查询用户角色名
        List<Long> userIds = users.stream().map(SysUser::getId).toList();
        List<SysUserRole> allUserRoles = userRoleRepository.findByUserIdInAndIsDeletedFalse(userIds);
        List<Long> roleIds = allUserRoles.stream().map(SysUserRole::getRoleId).distinct().toList();

        final Map<Long, String> roleIdToName = roleIds.isEmpty()
                ? Collections.emptyMap()
                : roleRepository.findByIdInAndIsDeletedFalse(roleIds).stream()
                        .collect(Collectors.toMap(SysRole::getId, SysRole::getRoleName));

        Map<Long, List<String>> userRoleNames = allUserRoles.stream()
                .collect(Collectors.groupingBy(
                        SysUserRole::getUserId,
                        Collectors.mapping(ur -> roleIdToName.getOrDefault(ur.getRoleId(), ""), Collectors.toList())
                ));

        // 批量检查在线状态
        Set<Long> onlineUserIds = userIds.stream()
                .filter(uid -> Boolean.TRUE.equals(redisTemplate.hasKey(CacheConstant.ONLINE_USER_PREFIX + uid)))
                .collect(Collectors.toSet());

        // 批量查询机构名称
        List<String> orgCodes = users.stream()
                .map(SysUser::getOrgCode)
                .filter(c -> c != null)
                .distinct()
                .toList();
        Map<String, String> orgCodeToName = orgCodes.isEmpty()
                ? Collections.emptyMap()
                : orgRepository.findByOrgCodeInAndIsDeletedFalse(orgCodes).stream()
                        .collect(Collectors.toMap(SysOrg::getOrgCode, SysOrg::getOrgShortName));

        List<UserResponse> responses = users.stream()
                .map(user -> userConverter.toResponse(
                        user,
                        orgCodeToName.get(user.getOrgCode()),
                        userRoleNames.getOrDefault(user.getId(), Collections.emptyList()),
                        onlineUserIds.contains(user.getId())))
                .toList();

        return new PageImpl<>(responses, pageRequest, page.getTotalElements());
    }

    @Override
    @Transactional
    public String resetPassword(Long id, UserResetPasswordRequest request) {
        SysUser user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("用户不存在: " + id));

        int strengthLevel = getConfigInt("password_strength_level", 1);
        String newPassword;
        if (request.getNewPassword() != null && !request.getNewPassword().isBlank()) {
            UserConverter.validatePasswordStrength(request.getNewPassword(), strengthLevel);
            newPassword = request.getNewPassword();
        } else {
            newPassword = UserConverter.generateRandomPassword(strengthLevel);
        }

        user.setPassword(Md5Util.encrypt(newPassword));
        user.setLoginFailCount(0);
        user.setPasswordUpdateTime(LocalDateTime.now());
        userRepository.save(user);

        return newPassword;
    }

    @Override
    @Transactional
    public void changePassword(UserChangePasswordRequest request) {
        Long userId = LoginUserContext.getUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }

        SysUser user = userRepository.findByIdAndIsDeletedFalse(userId)
                .orElseThrow(() -> new BizException("用户不存在: " + userId));

        if (!user.getPassword().equals(Md5Util.encrypt(request.getOldPassword()))) {
            throw new BizException("旧密码错误");
        }

        int strengthLevel = getConfigInt("password_strength_level", 1);
        UserConverter.validatePasswordStrength(request.getNewPassword(), strengthLevel);

        user.setPassword(Md5Util.encrypt(request.getNewPassword()));
        user.setLoginFailCount(0);
        user.setPasswordUpdateTime(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unlock(Long id) {
        SysUser user = userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("用户不存在: " + id));
        user.setStatus(dictService.getDictValue(CommonStatus.class.getSimpleName(), CommonStatus.ENABLED.name()));
        user.setLoginFailCount(0);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void assignRoles(Long id, UserAssignRolesRequest request) {
        userRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("用户不存在: " + id));

        for (Long roleId : request.getRoleIds()) {
            roleRepository.findByIdAndIsDeletedFalse(roleId)
                    .orElseThrow(() -> new BizException("角色不存在: " + roleId));
        }

        List<SysUserRole> oldRoles = userRoleRepository.findByUserIdAndIsDeletedFalse(id);
        oldRoles.forEach(ur -> ur.setIsDeleted(true));
        if (!oldRoles.isEmpty()) {
            userRoleRepository.saveAll(oldRoles);
        }

        List<SysUserRole> newRoles = request.getRoleIds().stream()
                .map(roleId -> SysUserRole.builder().userId(id).roleId(roleId).build())
                .toList();
        if (!newRoles.isEmpty()) {
            userRoleRepository.saveAll(newRoles);
        }
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
