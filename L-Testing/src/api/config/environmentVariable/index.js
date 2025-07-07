import request from "@/utils/request";

export const environmentVariableApis = {
    saveEnvironmentVariable: (data) => {
        return request({
            url: '/config/environmentVariable/add',
            method: 'post',
            data
        })
    },
    getUserEnvironmentVariable: () => {
        return request({
            url: '/config/environmentVariable/get',
            method: 'get'
        })
    },
    deleteUserEnvironmentVariable: (id) => {
        return request({
            url: '/config/environmentVariable/delete',
            method: 'post',
            params: {
                id
            }
        })
    },
    updateEnvironmentVariable: (data) => {
        return request({
            url: '/config/environmentVariable/update',
            method: 'post',
            data
        })
    }
}
