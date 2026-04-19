package com.cyz.entity;

import com.cyz.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sys_menu", uniqueConstraints = {
        @UniqueConstraint(name = "uk_menu_code", columnNames = "menu_code")
}, indexes = {
        @Index(name = "idx_menu_parent_id", columnList = "parent_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysMenu extends BaseEntity {

    @Column(name = "menu_code", length = 64, nullable = false, unique = true)
    private String menuCode;

    @Column(name = "menu_name", length = 64, nullable = false)
    private String menuName;

    @Column(name = "menu_type", length = 32, nullable = false)
    private String menuType;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "path", length = 255)
    private String path;

    @Column(name = "component", length = 255)
    private String component;

    @Column(name = "perms", length = 255)
    private String perms;

    @Column(name = "icon", length = 128)
    private String icon;

    @Column(name = "is_frame")
    private Boolean isFrame;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "status", length = 32)
    private String status;
}
