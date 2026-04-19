package com.cyz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "sys_login_log_archive")
public class SysLoginLogArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", length = 64)
    private String username;

    @Column(name = "login_ip", length = 64)
    private String loginIp;

    @Column(name = "login_device", length = 255)
    private String loginDevice;

    @Column(name = "login_type", length = 32)
    private String loginType;

    @Column(name = "login_result", length = 32)
    private String loginResult;

    @Column(name = "fail_reason", length = 255)
    private String failReason;

    @Column(name = "login_time")
    private LocalDateTime loginTime;

    @Column(name = "logout_time")
    private LocalDateTime logoutTime;

    @Column(name = "is_abnormal")
    private Boolean isAbnormal = false;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "create_by")
    private Long createBy;

    @Column(name = "update_by")
    private Long updateBy;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
