package com.lmj.platformserver.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.lmj.platformserver.context.UserContextHolder;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        Long userId = UserContextHolder.getUserId();
        this.strictInsertFill(metaObject, "createUser", Long.class, userId);
        this.strictInsertFill(metaObject, "createdTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateUser", Long.class, userId);
        this.strictInsertFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long userId = UserContextHolder.getUserId();
        this.strictInsertFill(metaObject, "updateUser", Long.class, userId);
        this.strictUpdateFill(metaObject, "updatedTime", LocalDateTime.class, LocalDateTime.now());
    }
}
