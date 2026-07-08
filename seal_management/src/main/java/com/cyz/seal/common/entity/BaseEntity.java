package com.cyz.seal.common.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 业务实体的公共基类：审计字段 + 逻辑删除。
 *
 * <p><b>关于 legal_entity_id</b>：多租户行级隔离字段（ADR-0002）<b>不</b>在此声明为映射属性——
 * 它由 {@code TenantLineInnerInterceptor} 在 SQL 层统一注入（INSERT 补列、SELECT/WHERE/UPDATE 加条件），
 * 若同时在实体上映射，INSERT 会产生重复列。需要回读时以只读投影方式单独查询。
 */
@Data
public abstract class BaseEntity {

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人用户 ID（由 MetaObjectHandler 填充，依赖认证上下文，TODO：IAM 接入后补）。 */
    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private Long createBy;

    /** 更新人用户 ID（同上）。 */
    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private Long updateBy;

    /** 逻辑删除标记：0 未删除，1 已删除。 */
    @TableLogic
    @TableField(value = "deleted")
    private Integer deleted;
}
