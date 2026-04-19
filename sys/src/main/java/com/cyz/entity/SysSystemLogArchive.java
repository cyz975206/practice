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
@Table(name = "sys_system_log_archive")
public class SysSystemLogArchive {

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

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
