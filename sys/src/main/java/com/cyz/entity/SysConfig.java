package com.cyz.entity;

import com.cyz.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "sys_config", uniqueConstraints = {
        @UniqueConstraint(name = "uk_config_key", columnNames = "config_key")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SysConfig extends BaseEntity {

    @Column(name = "config_key", length = 64, nullable = false, unique = true)
    private String configKey;

    @Column(name = "config_name", length = 64)
    private String configName;

    @Column(name = "config_value", length = 255)
    private String configValue;

    @Column(name = "remark", length = 255)
    private String remark;

    @Column(name = "sort")
    private Integer sort;
}
