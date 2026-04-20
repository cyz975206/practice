package com.cyz.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysTaskLogDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long taskId;
    private String taskName;
    private String serviceName;
    private String funPath;
    private String cron;
    private Integer runResult;
    private String runLog;
    private Long durationMs;
}
