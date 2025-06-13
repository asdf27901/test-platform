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
    },
    addUser: (params) => {
        return request({
            url: '/user/add',
            method: 'post',
            data: params
        })
    },
    updateUser: (params) => {
        return request({
            url: '/user/update',
            method: 'post',
            data: params
        })
    }
}
