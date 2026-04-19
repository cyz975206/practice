package com.cyz.service;

import com.cyz.dto.request.RoleCreateRequest;
import com.cyz.dto.request.RoleQueryRequest;
import com.cyz.dto.request.RoleUpdateRequest;
import com.cyz.dto.response.RoleResponse;
import org.springframework.data.domain.Page;

import java.util.List;

public interface SysRoleService {

    RoleResponse create(RoleCreateRequest request);

    RoleResponse update(Long id, RoleUpdateRequest request);

    void delete(Long id);

    RoleResponse getById(Long id);

    Page<RoleResponse> list(RoleQueryRequest request);

    void assignMenus(Long id, List<Long> menuIds);

    List<Long> getMenuIds(Long id);
}
