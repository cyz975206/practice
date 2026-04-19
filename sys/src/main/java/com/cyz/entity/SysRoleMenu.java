package com.cyz.entity;

import com.cyz.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sys_role_menu", uniqueConstraints = {
        @UniqueConstraint(name = "uk_role_menu", columnNames = {"role_id", "menu_id"})
}, indexes = {
        @Index(name = "idx_role_menu_menu_id", columnList = "menu_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysRoleMenu extends BaseEntity {

    @Column(name = "role_id", nullable = false)
    private Long roleId;

    @Column(name = "menu_id", nullable = false)
    private Long menuId;
}
