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
@Table(name = "sys_security_log_archive")
public class SysSecurityLogArchive {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 64)
    private String operatorName;

    @Column(name = "operator_ip", length = 64)
    private String operatorIp;

    @Column(name = "risk_type", length = 64)
    private String riskType;

    @Column(name = "risk_content", length = 512)
    private String riskContent;

    @Column(name = "request_url", length = 255)
    private String requestUrl;

    @Column(name = "risk_level", length = 32)
    private String riskLevel;

    @Column(name = "handle_status", length = 32)
    private String handleStatus;

    @Column(name = "handle_user_id")
    private Long handleUserId;

    @Column(name = "handle_time")
    private LocalDateTime handleTime;

    @Column(name = "handle_note", length = 512)
    private String handleNote;

    @Column(name = "occur_time")
    private LocalDateTime occurTime;

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
