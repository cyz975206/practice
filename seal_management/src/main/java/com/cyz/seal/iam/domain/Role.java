package com.cyz.seal.iam.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyz.seal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色（按法人实体隔离）。legal_entity_id 由多租户拦截器注入（实体不映射）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_role")
public class Role extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 角色编码（法人实体内唯一）。 */
    private String code;

    /** 角色名称。 */
    private String name;

    /** 作用域（ENTITY/GROUP）。 */
    private RoleScope scope;

    /** 状态：1 启用 0 停用。 */
    private Integer status;
}
