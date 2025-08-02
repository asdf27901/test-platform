import request from "@/utils/request";

export const dataBoardApis = {
    getData: () => {
        return request({
            url: '/dataBoard/getData',
            method: 'get'
        })
    }
}
