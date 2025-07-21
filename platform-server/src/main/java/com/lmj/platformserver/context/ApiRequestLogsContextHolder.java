package com.lmj.platformserver.context;

import com.lmj.platformserver.entity.ApiRequestLogs;

public class ApiRequestLogsContextHolder {

    private static final ThreadLocal<ApiRequestLogs> contextHolder = new ThreadLocal<>();

    public static void set(ApiRequestLogs apiRequestLogs) {
        contextHolder.set(apiRequestLogs);
    }

    public static ApiRequestLogs get() {
        return contextHolder.get();
    }

    public static void remove() {
        contextHolder.remove();
    }
}
