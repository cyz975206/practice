package com.cyz.converter;

import com.cyz.dto.request.MenuCreateRequest;
import com.cyz.dto.request.MenuUpdateRequest;
import com.cyz.dto.response.MenuResponse;
import com.cyz.dto.response.MenuTreeResponse;
import com.cyz.entity.SysMenu;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class MenuConverter {

    public SysMenu toEntity(MenuCreateRequest request) {
        return SysMenu.builder()
                .menuCode(request.getMenuCode())
                .menuName(request.getMenuName())
                .menuType(request.getMenuType())
                .parentId(request.getParentId())
                .path(request.getPath())
                .component(request.getComponent())
                .perms(request.getPerms())
                .icon(request.getIcon())
                .isFrame(request.getIsFrame() != null ? request.getIsFrame() : false)
                .sort(request.getSort() != null ? request.getSort() : 0)
                .status(request.getStatus() != null ? request.getStatus() : "1")
                .build();
    }

    public void updateEntity(SysMenu entity, MenuUpdateRequest request) {
        if (request.getMenuName() != null) {
            entity.setMenuName(request.getMenuName());
        }
        if (request.getMenuType() != null) {
            entity.setMenuType(request.getMenuType());
        }
        if (request.getParentId() != null) {
            entity.setParentId(request.getParentId());
        }
        if (request.getPath() != null) {
            entity.setPath(request.getPath());
        }
        if (request.getComponent() != null) {
            entity.setComponent(request.getComponent());
        }
        if (request.getPerms() != null) {
            entity.setPerms(request.getPerms());
        }
        if (request.getIcon() != null) {
            entity.setIcon(request.getIcon());
        }
        if (request.getIsFrame() != null) {
            entity.setIsFrame(request.getIsFrame());
        }
        if (request.getSort() != null) {
            entity.setSort(request.getSort());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
    }

    public MenuResponse toResponse(SysMenu entity) {
        MenuResponse response = new MenuResponse();
        response.setId(entity.getId());
        response.setMenuCode(entity.getMenuCode());
        response.setMenuName(entity.getMenuName());
        response.setMenuType(entity.getMenuType());
        response.setParentId(entity.getParentId());
        response.setPath(entity.getPath());
        response.setComponent(entity.getComponent());
        response.setPerms(entity.getPerms());
        response.setIcon(entity.getIcon());
        response.setIsFrame(entity.getIsFrame());
        response.setSort(entity.getSort());
        response.setStatus(entity.getStatus());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());
        return response;
    }

    public MenuTreeResponse toTreeResponse(SysMenu entity) {
        MenuTreeResponse response = new MenuTreeResponse();
        response.setId(entity.getId());
        response.setMenuCode(entity.getMenuCode());
        response.setMenuName(entity.getMenuName());
        response.setMenuType(entity.getMenuType());
        response.setParentId(entity.getParentId());
        response.setPath(entity.getPath());
        response.setComponent(entity.getComponent());
        response.setPerms(entity.getPerms());
        response.setIcon(entity.getIcon());
        response.setIsFrame(entity.getIsFrame());
        response.setSort(entity.getSort());
        response.setStatus(entity.getStatus());
        return response;
    }

    public List<MenuTreeResponse> buildTree(List<SysMenu> menus) {
        List<MenuTreeResponse> allNodes = menus.stream()
                .map(this::toTreeResponse)
                .collect(Collectors.toList());

        Map<Long, List<MenuTreeResponse>> childrenMap = allNodes.stream()
                .filter(n -> n.getParentId() != null)
                .collect(Collectors.groupingBy(MenuTreeResponse::getParentId));

        List<MenuTreeResponse> roots = allNodes.stream()
                .filter(n -> n.getParentId() == null)
                .collect(Collectors.toList());

        for (MenuTreeResponse root : roots) {
            setChildren(root, childrenMap);
        }

        return roots;
    }

    private void setChildren(MenuTreeResponse node, Map<Long, List<MenuTreeResponse>> childrenMap) {
        List<MenuTreeResponse> children = childrenMap.get(node.getId());
        if (children != null && !children.isEmpty()) {
            node.setChildren(children);
            for (MenuTreeResponse child : children) {
                setChildren(child, childrenMap);
            }
        }
    }
}
