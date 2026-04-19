package com.cyz.service.impl;

import com.cyz.common.exception.BizException;
import com.cyz.converter.RoleConverter;
import com.cyz.dto.request.RoleCreateRequest;
import com.cyz.dto.request.RoleQueryRequest;
import com.cyz.dto.request.RoleUpdateRequest;
import com.cyz.dto.response.RoleResponse;
import com.cyz.entity.SysRole;
import com.cyz.entity.SysRoleMenu;
import com.cyz.entity.SysUserRole;
import com.cyz.repository.SysRoleMenuRepository;
import com.cyz.repository.SysRoleRepository;
import com.cyz.repository.SysUserRoleRepository;
import com.cyz.service.SysRoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SysRoleServiceImpl implements SysRoleService {

    private final SysRoleRepository roleRepository;
    private final SysRoleMenuRepository roleMenuRepository;
    private final SysUserRoleRepository userRoleRepository;
    private final RoleConverter roleConverter;

    @Override
    @Transactional
    public RoleResponse create(RoleCreateRequest request) {
        if (roleRepository.existsByRoleCodeAndIsDeletedFalse(request.getRoleCode())) {
            throw new BizException("角色编码已存在: " + request.getRoleCode());
        }

        SysRole role = roleConverter.toEntity(request);
        SysRole saved = roleRepository.save(role);
        return roleConverter.toResponse(saved);
    }

    @Override
    @Transactional
    public RoleResponse update(Long id, RoleUpdateRequest request) {
        SysRole role = roleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("角色不存在: " + id));

        roleConverter.updateEntity(role, request);
        SysRole saved = roleRepository.save(role);
        return roleConverter.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        SysRole role = roleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("角色不存在: " + id));

        role.setIsDeleted(true);
        roleRepository.save(role);

        List<SysRoleMenu> roleMenus = roleMenuRepository.findByRoleIdAndIsDeletedFalse(id);
        roleMenus.forEach(rm -> rm.setIsDeleted(true));
        if (!roleMenus.isEmpty()) {
            roleMenuRepository.saveAll(roleMenus);
        }

        List<SysUserRole> userRoles = userRoleRepository.findByRoleIdAndIsDeletedFalse(id);
        userRoles.forEach(ur -> ur.setIsDeleted(true));
        if (!userRoles.isEmpty()) {
            userRoleRepository.saveAll(userRoles);
        }
    }

    @Override
    public RoleResponse getById(Long id) {
        SysRole role = roleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("角色不存在: " + id));
        return roleConverter.toResponse(role);
    }

    @Override
    public Page<RoleResponse> list(RoleQueryRequest request) {
        PageRequest pageRequest = PageRequest.of(request.getPage() - 1, request.getSize());
        Page<SysRole> page = roleRepository.findByConditions(
                request.getRoleName(), request.getStatus(), pageRequest);
        return page.map(roleConverter::toResponse);
    }

    @Override
    @Transactional
    public void assignMenus(Long id, List<Long> menuIds) {
        roleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("角色不存在: " + id));

        roleMenuRepository.deleteByRoleId(id);

        List<SysRoleMenu> newMenus = menuIds.stream()
                .map(menuId -> SysRoleMenu.builder().roleId(id).menuId(menuId).build())
                .toList();
        if (!newMenus.isEmpty()) {
            roleMenuRepository.saveAll(newMenus);
        }
    }

    @Override
    public List<Long> getMenuIds(Long id) {
        roleRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new BizException("角色不存在: " + id));

        return roleMenuRepository.findByRoleIdAndIsDeletedFalse(id).stream()
                .map(SysRoleMenu::getMenuId)
                .collect(Collectors.toList());
    }
}
