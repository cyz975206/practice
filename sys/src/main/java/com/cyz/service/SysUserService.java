package com.cyz.service;

import com.cyz.dto.request.UserAssignRolesRequest;
import com.cyz.dto.request.UserChangePasswordRequest;
import com.cyz.dto.request.UserCreateRequest;
import com.cyz.dto.request.UserQueryRequest;
import com.cyz.dto.request.UserResetPasswordRequest;
import com.cyz.dto.request.UserUpdateRequest;
import com.cyz.dto.response.UserResponse;
import org.springframework.data.domain.Page;

public interface SysUserService {

    UserResponse create(UserCreateRequest request);

    UserResponse update(Long id, UserUpdateRequest request);

    void delete(Long id);

    UserResponse getById(Long id);

    Page<UserResponse> list(UserQueryRequest request);

    String resetPassword(Long id, UserResetPasswordRequest request);

    void changePassword(UserChangePasswordRequest request);

    void unlock(Long id);

    void assignRoles(Long id, UserAssignRolesRequest request);
}
