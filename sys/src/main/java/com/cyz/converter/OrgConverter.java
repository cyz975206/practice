package com.cyz.converter;

import com.cyz.common.util.PinyinUtil;
import com.cyz.dto.request.OrgCreateRequest;
import com.cyz.dto.request.OrgUpdateRequest;
import com.cyz.dto.response.OrgResponse;
import com.cyz.dto.response.OrgTreeResponse;
import com.cyz.entity.SysOrg;

import java.util.List;
import java.util.stream.Collectors;

public class OrgConverter {

    public static SysOrg toEntity(OrgCreateRequest request, String orgCode) {
        return SysOrg.builder()
                .orgCode(orgCode)
                .orgShortName(request.getOrgShortName())
                .orgFullName(request.getOrgFullName())
                .orgLevel(request.getOrgLevel())
                .parentOrgCode(request.getParentOrgCode())
                .leaderUserId(request.getLeaderUserId())
                .status(request.getStatus() != null ? request.getStatus() : "1")
                .sort(request.getSort() != null ? request.getSort() : 0)
                .build();
    }

    public static void updateEntity(SysOrg entity, OrgUpdateRequest request) {
        if (request.getOrgShortName() != null) {
            entity.setOrgShortName(request.getOrgShortName());
        }
        if (request.getOrgFullName() != null) {
            entity.setOrgFullName(request.getOrgFullName());
        }
        if (request.getOrgLevel() != null) {
            entity.setOrgLevel(request.getOrgLevel());
        }
        if (request.getParentOrgCode() != null) {
            entity.setParentOrgCode(request.getParentOrgCode());
        }
        if (request.getLeaderUserId() != null) {
            entity.setLeaderUserId(request.getLeaderUserId());
        }
        if (request.getStatus() != null) {
            entity.setStatus(request.getStatus());
        }
        if (request.getSort() != null) {
            entity.setSort(request.getSort());
        }
    }

    public static OrgResponse toResponse(SysOrg entity) {
        OrgResponse response = new OrgResponse();
        response.setId(entity.getId());
        response.setOrgCode(entity.getOrgCode());
        response.setOrgShortName(entity.getOrgShortName());
        response.setOrgFullName(entity.getOrgFullName());
        response.setOrgLevel(entity.getOrgLevel());
        response.setParentOrgCode(entity.getParentOrgCode());
        response.setLeaderUserId(entity.getLeaderUserId());
        response.setStatus(entity.getStatus());
        response.setSort(entity.getSort());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());
        return response;
    }

    public static OrgTreeResponse toTreeResponse(SysOrg entity) {
        OrgTreeResponse response = new OrgTreeResponse();
        response.setId(entity.getId());
        response.setOrgCode(entity.getOrgCode());
        response.setOrgShortName(entity.getOrgShortName());
        response.setOrgFullName(entity.getOrgFullName());
        response.setOrgLevel(entity.getOrgLevel());
        response.setParentOrgCode(entity.getParentOrgCode());
        response.setLeaderUserId(entity.getLeaderUserId());
        response.setStatus(entity.getStatus());
        response.setSort(entity.getSort());
        return response;
    }

    /**
     * 将机构列表构建为树形结构
     */
    public static List<OrgTreeResponse> buildTree(List<SysOrg> orgs) {
        List<OrgTreeResponse> allNodes = orgs.stream()
                .map(OrgConverter::toTreeResponse)
                .collect(Collectors.toList());

        // 用Map按parentOrgCode分组
        java.util.Map<String, List<OrgTreeResponse>> childrenMap = allNodes.stream()
                .filter(n -> n.getParentOrgCode() != null)
                .collect(Collectors.groupingBy(OrgTreeResponse::getParentOrgCode));

        // 找到根节点（parentOrgCode为null或为空的节点）
        List<OrgTreeResponse> roots = allNodes.stream()
                .filter(n -> n.getParentOrgCode() == null || n.getParentOrgCode().isEmpty())
                .collect(Collectors.toList());

        // 递归设置children
        for (OrgTreeResponse root : roots) {
            setChildren(root, childrenMap);
        }

        return roots;
    }

    private static void setChildren(OrgTreeResponse node, java.util.Map<String, List<OrgTreeResponse>> childrenMap) {
        List<OrgTreeResponse> children = childrenMap.get(node.getOrgCode());
        if (children != null && !children.isEmpty()) {
            node.setChildren(children);
            for (OrgTreeResponse child : children) {
                setChildren(child, childrenMap);
            }
        }
    }
}
