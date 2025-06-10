import request from '@/utils/request';

/**
 * 登录api接口集合
 * @method login 用户登录
 * @method getCaptcha 获取验证码
 */
export function useLoginApi() {
	return {
		login: (params) => {
			return request({
				url: '/login',
				method: 'post',
				withCredentials: true,
				data: params

			});
		},
		getCaptcha: () => {
			return request({
				url: '/login/captcha',
				method: 'get',
				withCredentials: true,
			})
		}
	};
}
