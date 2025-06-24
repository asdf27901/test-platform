import request from "@/utils/request";

export const fileApis = {
    uploadTestcaseFile: (file) => {
        const formData = new FormData();
        formData.append('file', file);
        return request({
            url: '/file/uploadTestcaseFile',
            method: 'post',
            data: formData
        })
    },
    uploadImage: (file) => {
        return request({
            url: '/file/uploadImage',
            method: 'post',
            data: {
                file
            }
        })
    }
}
