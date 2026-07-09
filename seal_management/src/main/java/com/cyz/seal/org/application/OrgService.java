package com.cyz.seal.org.application;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyz.seal.org.domain.Org;
import com.cyz.seal.org.interfaces.dto.OrgCreateRequest;
import com.cyz.seal.org.interfaces.dto.OrgUpdateRequest;

import java.util.List;

/**
 * 部门 / 机构应用服务（本法人实体范围内的机构树管理）。
 */
public interface OrgService extends IService<Org> {

    /** 新建机构：校验编码唯一 + 父机构合法，生成 ancestors。 */
    Org create(OrgCreateRequest req);

    /** 更新机构：可改名称/排序/状态；改父机构时连带重算自身及子孙的 ancestors（防成环）。 */
    Org update(Long id, OrgUpdateRequest req);

    /** 停用机构。 */
    void disable(Long id);

    /** 取本法人实体的全部机构（按 sort 排序，供前端据 parentId/ancestors 组树）。 */
    List<Org> listTree();
}
