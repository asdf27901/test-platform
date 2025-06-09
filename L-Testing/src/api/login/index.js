import request from '@/utils/request';

/**
 * 登录api接口集合
 * @method signIn 用户登录
 */
export function useLoginApi() {
	return {
		signIn: (params) => {
			return request({
				url: '/user/signIn',
				method: 'post',
				data: params,
			});
		}
	};
}
