package com.cyz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "sys_task_log", indexes = {
        @Index(name = "idx_task_log_task_id", columnList = "task_id"),
        @Index(name = "idx_task_log_fun_path", columnList = "fun_path"),
        @Index(name = "idx_task_log_create_time", columnList = "create_time")
})
public class SysTaskLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "task_id")
    private Long taskId;

    @Column(name = "task_name", length = 64)
    private String taskName;

    @Column(name = "service_name", length = 64)
    private String serviceName;

    @Column(name = "fun_path", length = 255)
    private String funPath;

    @Column(name = "cron", length = 32)
    private String cron;

    @Column(name = "run_result")
    private Integer runResult;

    @Column(name = "run_log", columnDefinition = "text")
    private String runLog;

    @Column(name = "duration_ms")
    private Long durationMs;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
