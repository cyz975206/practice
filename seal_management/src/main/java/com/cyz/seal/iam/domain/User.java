package com.cyz.seal.iam.domain;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyz.seal.common.entity.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户（按法人实体隔离）。
 *
 * <p>legalEntityId 为<b>只读映射</b>：{@code insertStrategy/updateStrategy=NEVER} 使其不出现在
 * INSERT/UPDATE（由多租户拦截器在 SQL 层注入），但可 SELECT 回读（登录时需取它写进 JWT）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属法人实体（只读回读，由拦截器写入）。 */
    @TableField(value = "legal_entity_id", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER)
    private Long legalEntityId;

    /** 登录名（全局唯一，便于登录消歧）。 */
    private String username;

    /** BCrypt 密码哈希（不序列化到前端）。 */
    @JsonIgnore
    private String password;

    /** 昵称。 */
    private String nickname;

    /** 状态：1 启用 0 停用。 */
    private Integer status;
}
