import request from "@/utils/request";

export const LogsApi = {
    getApiRequestLogsList: (params) => {
        return request({
            url: '/apiRequestLogs/list',
            method: 'get',
            params
        })
    },
    getApiRequestLogById: (id) => {
        return request({
            url: '/apiRequestLogs/detail',
            method: 'get',
            params: {
                id
            }
        })
    }
}
