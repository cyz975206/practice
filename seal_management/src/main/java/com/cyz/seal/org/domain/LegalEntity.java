package com.cyz.seal.org.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.cyz.seal.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 法人实体（多租户行级隔离的租户单位，ADR-0002）。
 *
 * <p>全局表：本身不参与 legal_entity_id 过滤（见 {@code MybatisPlusConfig.ignoreTable}）。
 * id 由 MyBatis-Plus 雪花分配（ASSIGN_ID，分布式友好，ADR-0009）。
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("legal_entity")
public class LegalEntity extends BaseEntity {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 所属集团（一个部署 = 一个集团；集团表后续补，骨架先留字段）。 */
    private Long groupId;

    /** 法人实体编码（唯一）。 */
    private String code;

    /** 法人全称。 */
    private String fullName;

    /** 法人简称。 */
    private String shortName;

    /** 类型。 */
    private EntityType entityType;

    /** 状态：1 启用 0 停用。 */
    private Integer status;
}
