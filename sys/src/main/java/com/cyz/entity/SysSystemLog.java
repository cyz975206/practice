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
@Table(name = "sys_system_log", indexes = {
        @Index(name = "idx_system_log_occur_time", columnList = "occur_time")
})
public class SysSystemLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_level", length = 32)
    private String logLevel;

    @Column(name = "log_module", length = 64)
    private String logModule;

    @Column(name = "log_content", columnDefinition = "text")
    private String logContent;

    @Column(name = "exception_stack", columnDefinition = "text")
    private String exceptionStack;

    @Column(name = "occur_time")
    private LocalDateTime occurTime;

    @Column(name = "server_ip", length = 64)
    private String serverIp;

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
