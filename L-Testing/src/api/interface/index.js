import request from '@/utils/request';

export const interfaceApis = {

    addInterfaceBatch: (interfaces) => {
        return request({
            url: '/interface/saveInterfaces',
            method: 'post',
            data: {
                interfaces
            }
        })
    },
    getInterfaceList: (params) => {
        return request({
            url: '/interface/interfaceList',
            method: 'get',
            params
        })
    },
    deleteInterfacesBatch: (ids) => {
        return request({
            url: '/interface/deleteBatch',
            method: 'post',
            data: {
                ids
            }
        })
    },
    updateInterface: (params) => {
        return request({
            url: '/interface/update',
            method: 'post',
            data: params
        })
    }
}
