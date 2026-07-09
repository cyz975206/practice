package com.cyz.seal.iam.application;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyz.seal.iam.domain.Role;
import com.cyz.seal.iam.interfaces.dto.RoleCreateRequest;
import com.cyz.seal.iam.interfaces.dto.RoleUpdateRequest;

public interface RoleService extends IService<Role> {

    /** 创建角色（编码在本法人实体内唯一）。 */
    Role create(RoleCreateRequest req);

    /** 更新角色（name/status；code 与 scope 锁定不可改）。 */
    Role update(Long id, RoleUpdateRequest req);

    /** 停用角色。 */
    void disable(Long id);
}
