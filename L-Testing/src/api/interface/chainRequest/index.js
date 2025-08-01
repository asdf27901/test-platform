import request from "@/utils/request";

export const chainRequestApis = {
    saveChainRequest: data => {
        return request({
            url: '/chainRequest/save',
            method: 'post',
            data
        })
    },
    getChainRequestList: params => {
        return request({
            url: '/chainRequest/list',
            method: 'get',
            params
        })
    },
    deleteChainRequest: data => {
        return request({
            url: '/chainRequest/delete',
            method: 'post',
            data
        })
    },
    getChainRequestDetail: params => {
        return request({
            url: '/chainRequest/detail',
            method: 'get',
            params
        })
    },
    updateChainRequest: data => {
        return request({
            url: '/chainRequest/update',
            method: 'post',
            data
        })
    },
    executeChainRequest: params => {
        return request({
            url: '/chainRequest/executeChainRequest',
            method: 'post',
            params
        })
    }
}
