package com.lmj.platformserver.context;

public class UserContextHolder {

    private static final ThreadLocal<Long> userIdHolder = new ThreadLocal<>();

    /**
     * 保存 userId 到 ThreadLocal
     */
    public static void setUserId(Long userId) {
        userIdHolder.set(userId);
    }

    /**
     * 从 ThreadLocal 获取 userId
     */
    public static Long getUserId() {
        return userIdHolder.get();
    }

    /**
     * 清理 ThreadLocal，防止内存泄漏
     */
    public static void clear() {
        userIdHolder.remove();
    }
}
