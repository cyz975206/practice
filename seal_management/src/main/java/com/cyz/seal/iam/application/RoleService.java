package com.cyz.seal.iam.application;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyz.seal.iam.domain.Role;
import com.cyz.seal.iam.interfaces.dto.RoleCreateRequest;

public interface RoleService extends IService<Role> {

    /** 创建角色（编码在本法人实体内唯一）。 */
    Role create(RoleCreateRequest req);
}
