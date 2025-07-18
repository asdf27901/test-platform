import request from "@/utils/request";

export const interfaceTestcaseApis = {
    saveInterfaceTestcases: (data, envId) => {
        return request({
            url: '/interface/testcase/save',
            method: 'post',
            data,
            params: {
                envId
            }
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
    sendInterfaceTestcaseRequest: (requestData, envId, testcaseId) => {
        return request({
            url: `/interface/testcase/sendRequest`,
            method: 'post',
            data: requestData,
            params: {
                envId,
                testcaseId
            }
        })
    }
}
