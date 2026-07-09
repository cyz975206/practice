package com.cyz.seal.org.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyz.seal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门 / 机构（按法人实体隔离的租户表，ADR-0002）。
 *
 * <p>树形结构：{@code parentId} 自引用（0=顶级），{@code ancestors} 为物化路径
 *（"0," / "0,100,"…），便于查子树与祖先链，移动子树时整体重算。
 * legal_entity_id 由多租户拦截器注入（实体不映射）。
 * 统一平台同步预留 external_id / sync_source（ADR-0007），本期不接入。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_org")
public class Org extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 父机构 ID，0=顶级。 */
    private Long parentId;

    /** 机构编码（法人实体内唯一）。 */
    private String code;

    /** 机构名称。 */
    private String name;

    /** 祖先链物化路径，如 "0," 或 "0,100,"。 */
    private String ancestors;

    /** 显示排序。 */
    private Integer sort;

    /** 状态：1 启用 0 停用。 */
    private Integer status;

    /** 统一平台稳定 ID（同步预留）。 */
    private String externalId;

    /** 同步来源（同步预留）。 */
    private String syncSource;
}
