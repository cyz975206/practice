package com.cyz.entity;

import com.cyz.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sys_role", uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_code", columnNames = "role_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysRole extends BaseEntity {

    @Column(name = "role_code", length = 64, nullable = false, unique = true)
    private String roleCode;

    @Column(name = "role_name", length = 64, nullable = false)
    private String roleName;

    @Column(name = "role_desc", length = 255)
    private String roleDesc;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "sort")
    private Integer sort;
}
