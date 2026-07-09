package com.cyz.seal.iam.application;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cyz.seal.iam.domain.User;
import com.cyz.seal.iam.interfaces.dto.UserCreateRequest;

import java.util.List;

public interface UserService extends IService<User> {

    /** 创建用户（username 全局唯一；归属当前法人实体）。 */
    User create(UserCreateRequest req);

    /** 全量分配角色（本法人实体内）。 */
    void assignRoles(Long userId, List<Long> roleIds);
}
