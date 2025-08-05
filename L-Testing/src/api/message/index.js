import request from "@/utils/request";

export const messageApis = {
    getRecentMessageList: () => {
        return request({
            url: '/message/getRecentMessageList',
            method: 'get'
        })
    },
    getAllUnReadMessage: (params) => {
        return request({
            url: '/message/getAllUnReadMessage',
            method: 'get',
            params
        })
    },
    markMessageAsRead: (ids) => {
        return request({
            url: '/message/markMessageAsRead',
            method: 'post',
            params: ids
        })
    }
}
