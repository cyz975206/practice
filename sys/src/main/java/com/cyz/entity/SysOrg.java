package com.cyz.entity;

import com.cyz.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sys_org", uniqueConstraints = {
        @UniqueConstraint(name = "uk_org_code", columnNames = "org_code")
}, indexes = {
        @Index(name = "idx_org_parent_org_code", columnList = "parent_org_code")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysOrg extends BaseEntity {

    @Column(name = "org_code", length = 64, nullable = false, unique = true)
    private String orgCode;

    @Column(name = "org_short_name", length = 64)
    private String orgShortName;

    @Column(name = "org_full_name", length = 255)
    private String orgFullName;

    @Column(name = "org_level", length = 32)
    private String orgLevel;

    @Column(name = "parent_org_code", length = 64)
    private String parentOrgCode;

    @Column(name = "leader_user_id")
    private Long leaderUserId;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "sort")
    private Integer sort;
}
