package com.cyz.converter;

import com.cyz.dto.request.RoleCreateRequest;
import com.cyz.dto.request.RoleUpdateRequest;
import com.cyz.dto.response.RoleResponse;
import com.cyz.entity.SysRole;
import org.springframework.stereotype.Component;

@Component
public class RoleConverter {

    public SysRole toEntity(RoleCreateRequest request) {
        return SysRole.builder()
                .roleCode(request.getRoleCode())
                .roleName(request.getRoleName())
                .roleDesc(request.getRoleDesc())
                .status(request.getStatus() != null ? request.getStatus() : "1")
                .sort(request.getSort() != null ? request.getSort() : 0)
                .build();
    }

    public void updateEntity(SysRole entity, RoleUpdateRequest request) {
        if (request.getRoleName() != null) {
            entity.setRoleName(request.getRoleName());
        }
        if (request.getRoleDesc() != null) {
            entity.setRoleDesc(request.getRoleDesc());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getSort() != null) {
            entity.setSort(request.getSort());
        }
    }

    public RoleResponse toResponse(SysRole entity) {
        RoleResponse response = new RoleResponse();
        response.setId(entity.getId());
        response.setRoleCode(entity.getRoleCode());
        response.setRoleName(entity.getRoleName());
        response.setRoleDesc(entity.getRoleDesc());
        response.setStatus(entity.getStatus());
        response.setSort(entity.getSort());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());
        return response;
    }
}
