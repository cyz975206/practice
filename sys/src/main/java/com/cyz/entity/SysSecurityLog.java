package com.cyz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "sys_security_log", indexes = {
        @Index(name = "idx_security_log_occur_time", columnList = "occur_time"),
        @Index(name = "idx_security_log_handle_status", columnList = "handle_status")
})
public class SysSecurityLog {

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

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
