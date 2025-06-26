import request from "@/utils/request";

export const interfaceTestcaseApis = {
    saveInterfaceTestcases: (data) => {
        return request({
            url: '/interface/testcase/save',
            method: 'post',
            data
        })
    },
    getInterfaceTestcaseList: (params) => {
        return request({
            url: '/interface/testcase/list',
            method: 'get',
            params
        })
    },
    deleteInterfaceTestcaseBatch: (ids) => {
        return request({
            url: '/interface/testcase/delete',
            method: 'post',
            data: {
                ids
            }
        })
    },
    getInterfaceTestcaseDetail: (id) => {
        return request({
            url: '/interface/testcase/detail',
            method: 'get',
            params: {
                id
            }
        })
    },
    sendInterfaceTestcaseRequest: (requestData) => {
        return request({
            url: '/interface/testcase/sendRequest',
            method: 'post',
            data: requestData
        })
    }
}
