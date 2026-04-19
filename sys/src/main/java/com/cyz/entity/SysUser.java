package com.cyz.entity;

import com.cyz.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "sys_user", uniqueConstraints = {
        @UniqueConstraint(name = "uk_username", columnNames = "username")
}, indexes = {
        @Index(name = "idx_user_org_code", columnList = "org_code"),
        @Index(name = "idx_user_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysUser extends BaseEntity {

    @Column(name = "username", length = 64, nullable = false, unique = true)
    private String username;

    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @Column(name = "nickname", length = 64)
    private String nickname;

    @Column(name = "person_id")
    private Long personId;

    @Column(name = "org_code", length = 64)
    private String orgCode;

    @Column(name = "status", length = 32)
    private String status;

    @Column(name = "login_fail_count")
    private Integer loginFailCount;

    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    @Column(name = "password_update_time")
    private LocalDateTime passwordUpdateTime;
}
