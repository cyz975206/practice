package com.cyz.seal.org.application.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyz.seal.common.exception.BusinessException;
import com.cyz.seal.org.application.OrgService;
import com.cyz.seal.org.domain.Org;
import com.cyz.seal.org.infrastructure.persistence.mapper.OrgMapper;
import com.cyz.seal.org.interfaces.dto.OrgCreateRequest;
import com.cyz.seal.org.interfaces.dto.OrgUpdateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class OrgServiceImpl extends ServiceImpl<OrgMapper, Org> implements OrgService {

    private static final Long ROOT_PARENT = 0L;
    private static final String ROOT_ANCESTORS = "0,";

    @Override
    @Transactional
    public Org create(OrgCreateRequest req) {
        // 编码在本法人实体内唯一（拦截器按当前实体过滤）
        if (lambdaQuery().eq(Org::getCode, req.code()).exists()) {
            throw new BusinessException("机构编码已存在: " + req.code());
        }
        Long parentId = req.parentId() == null ? ROOT_PARENT : req.parentId();

        Org org = new Org();
        org.setParentId(parentId);
        org.setCode(req.code());
        org.setName(req.name());
        org.setAncestors(resolveAncestors(parentId));
        org.setSort(req.sort() == null ? 0 : req.sort());
        org.setStatus(1);
        save(org);
        return org;
    }

    @Override
    @Transactional
    public Org update(Long id, OrgUpdateRequest req) {
        Org org = getById(id);
        if (org == null) {
            throw new BusinessException("机构不存在: " + id);
        }
        if (req.name() != null) {
            org.setName(req.name());
        }
        if (req.sort() != null) {
            org.setSort(req.sort());
        }
        if (req.status() != null) {
            org.setStatus(req.status());
        }
        // 父机构变更：连带重算自身及子孙 ancestors（防成环）
        if (req.parentId() != null && !req.parentId().equals(org.getParentId())) {
            moveSubtree(org, req.parentId());
        }
        updateById(org);
        return org;
    }

    @Override
    @Transactional
    public void disable(Long id) {
        Org org = getById(id);
        if (org == null) {
            throw new BusinessException("机构不存在: " + id);
        }
        org.setStatus(0);
        updateById(org);
    }

    @Override
    public List<Org> listTree() {
        return lambdaQuery().orderByAsc(Org::getSort).list();
    }

    /** 计算 parentId 的 ancestors 路径；父机构非根时校验存在且同实体（拦截器过滤他实体）。 */
    private String resolveAncestors(Long parentId) {
        if (ROOT_PARENT.equals(parentId)) {
            return ROOT_ANCESTORS;
        }
        Org parent = getById(parentId);
        if (parent == null) {
            throw new BusinessException("父机构不存在: " + parentId);
        }
        return parent.getAncestors() + parentId + ",";
    }

    /**
     * 将 org 移到 newParentId 下，并同步重算所有子孙的 ancestors。
     * 防成环：newParentId 不能是 org 自身或其后代。
     */
    private void moveSubtree(Org org, Long newParentId) {
        if (newParentId.equals(org.getId())) {
            throw new BusinessException("不能将机构挂到自身下");
        }
        String oldAncestors = org.getAncestors();
        String newAncestors = ROOT_PARENT.equals(newParentId)
                ? ROOT_ANCESTORS
                : resolveAncestors(newParentId);

        // 防成环：新父不能是 org 的后代（其后代 ancestors 以 "<oldAncestors><orgId>," 开头）
        if (!ROOT_PARENT.equals(newParentId)
                && getById(newParentId).getAncestors().startsWith(oldAncestors + org.getId() + ",")) {
            throw new BusinessException("不能将机构挂到其子机构下（会成环）");
        }

        org.setParentId(newParentId);
        org.setAncestors(newAncestors);

        // 子孙：ancestors 以 "<oldAncestors><orgId>," 开头；把前缀 oldAncestors 替换为 newAncestors。
        List<Org> descendants = lambdaQuery()
                .likeRight(Org::getAncestors, oldAncestors + org.getId() + ",")
                .list();
        for (Org d : descendants) {
            d.setAncestors(d.getAncestors().replaceFirst(Pattern.quote(oldAncestors), newAncestors));
        }
        if (!descendants.isEmpty()) {
            updateBatchById(descendants);
        }
    }
}
