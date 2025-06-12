import request from '@/utils/request';

export const userApis = {
    getUserInfo: () => {
        return request({
            url: '/user/getUserInfo',
            method: 'get'
        })
    },
    getUserList: (params) => {
        return request({
            url: '/user/userList',
            method: 'get',
            params
        })
    }
}
