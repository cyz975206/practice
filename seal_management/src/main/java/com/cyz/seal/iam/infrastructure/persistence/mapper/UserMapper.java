package com.cyz.seal.iam.infrastructure.persistence.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.cyz.seal.iam.domain.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<User> {

    /**
     * 全局按 username 查询（忽略多租户过滤）：用于登录消歧 + username 全局唯一性校验。
     * 原因：登录/校验发生在租户上下文确定之前（context 为 null），此时拦截器会加
     * legal_entity_id = null 条件导致查不到，故此方法显式忽略租户线。
     */
    @InterceptorIgnore(tenantLine = "1")
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0")
    User selectByUsernameGlobal(@Param("username") String username);
}
