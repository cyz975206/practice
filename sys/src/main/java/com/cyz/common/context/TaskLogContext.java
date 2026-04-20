package com.cyz.common.context;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class TaskLogContext {

    private static final ThreadLocal<TaskLogInfo> CONTEXT_HOLDER = new ThreadLocal<>();

    public static void set(TaskLogInfo info) {
        CONTEXT_HOLDER.set(info);
    }

    public static TaskLogInfo get() {
        return CONTEXT_HOLDER.get();
    }

    public static void clear() {
        CONTEXT_HOLDER.remove();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskLogInfo {
        private Long taskId;
        private String taskName;
        private String serviceName;
        private String funPath;
        private String cron;
        private LocalDateTime startTime;
        private boolean logged;
    }
}
