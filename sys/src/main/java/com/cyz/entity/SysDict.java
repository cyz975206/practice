package com.cyz.entity;

import com.cyz.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sys_dict", uniqueConstraints = {
        @UniqueConstraint(name = "uk_dict_type_code", columnNames = {"dict_type", "dict_code"})
}, indexes = {
        @Index(name = "idx_dict_dict_type", columnList = "dict_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysDict extends BaseEntity {

    @Column(name = "dict_type", length = 64, nullable = false)
    private String dictType;

    @Column(name = "dict_name", length = 64)
    private String dictName;

    @Column(name = "dict_code", length = 64)
    private String dictCode;

    @Column(name = "dict_label", length = 64)
    private String dictLabel;

    @Column(name = "dict_value", length = 255)
    private String dictValue;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "remark", length = 255)
    private String remark;
}
