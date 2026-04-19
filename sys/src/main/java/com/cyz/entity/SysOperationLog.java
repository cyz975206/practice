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
@Table(name = "sys_operation_log", indexes = {
        @Index(name = "idx_operation_log_operation_time", columnList = "operation_time"),
        @Index(name = "idx_operation_log_operator_name", columnList = "operator_name"),
        @Index(name = "idx_operation_log_module", columnList = "module")
})
public class SysOperationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 64)
    private String operatorName;

    @Column(name = "operator_ip", length = 64)
    private String operatorIp;

    @Column(name = "operator_org_code", length = 64)
    private String operatorOrgCode;

    @Column(name = "operator_org", length = 64)
    private String operatorOrg;

    @Column(name = "module", length = 64)
    private String module;

    @Column(name = "operation_type", length = 32)
    private String operationType;

    @Column(name = "operation_content", length = 512)
    private String operationContent;

    @Column(name = "request_params", columnDefinition = "text")
    private String requestParams;

    @Column(name = "operation_result", length = 32)
    private String operationResult;

    @Column(name = "error_msg", columnDefinition = "text")
    private String errorMsg;

    @Column(name = "operation_time")
    private LocalDateTime operationTime;

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
