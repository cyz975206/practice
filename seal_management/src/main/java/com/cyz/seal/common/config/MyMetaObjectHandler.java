package com.cyz.seal.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.cyz.seal.common.context.CurrentUserContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 审计字段自动填充：create_time / update_time / create_by / update_by。
 *
 * <p>create_by / update_by 取当前用户 ID——TODO：IAM 接入后从 SecurityContext 取，
 * 骨架阶段先不填（null）。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        Long userId = currentUserId();
        if (userId != null) {
            strictInsertFill(metaObject, "createBy", Long.class, userId);
            strictInsertFill(metaObject, "updateBy", Long.class, userId);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        Long userId = currentUserId();
        if (userId != null) {
            strictUpdateFill(metaObject, "updateBy", Long.class, userId);
        }
    }

    /** 从当前登录用户上下文取用户 ID（请求线程内由 JWT 过滤器填充）。 */
    private Long currentUserId() {
        return CurrentUserContext.get();
    }
}
