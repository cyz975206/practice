package com.cyz.entity;

import com.cyz.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sys_task", uniqueConstraints = {
        @UniqueConstraint(name = "uk_task_service_fun", columnNames = {"service_name", "fun_path"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysTask extends BaseEntity {

    @Column(name = "name", length = 64, nullable = false)
    private String name;

    @Column(name = "service_name", length = 64, nullable = false)
    private String serviceName;

    @Column(name = "fun_path", length = 255, nullable = false)
    private String funPath;

    @Column(name = "cron", length = 32, nullable = false)
    private String cron;

    @Column(name = "has_start", nullable = false)
    private Boolean hasStart = false;
}
