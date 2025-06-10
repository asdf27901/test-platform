import axios from 'axios';
import { Message } from 'element-ui';
import { Local } from '@/utils/storage';

// 创建 axios 实例
const service = axios.create({
	baseURL: process.env.VUE_APP_BASE_API,
	timeout: 50000,
	// headers: { 'Content-Type': 'application/json' },
});

// 添加请求拦截器
service.interceptors.request.use(
	(config) => {
		// 在发送请求之前做些什么 token
		const token = Local.get('token')
		if (token) {
			config.headers.common['token'] = token;
		}
		return config;
	},
	(error) => {
		// 对请求错误做些什么
		return Promise.reject(error);
	}
);

// 添加响应拦截器
service.interceptors.response.use(
	(response) => {
		// 对响应数据做点什么
		const res = response.data;
		if (res.code && res.code !== 200) {
			if (res.code === 601 || res.code === 602) {
				Local.remove('token')
				window.location.href = '/login'
			}
			return Promise.reject(res);
		} else {
			return res
		}
	},
	(error) => {
		// 对响应错误做点什么
		if (error.response) {
			// HTTP状态码非2xx的情况
			const status = error.response.status;
			if (status === 401) {
				Message.error('未授权，请重新登录');
				// 可以执行登出逻辑，跳转登录页
			} else if (status === 403) {
				Message.error('拒绝访问');
			} else if (status === 404) {
				Message.error('请求资源不存在');
			} else if (status >= 500) {
				Message.error('服务器错误');
			} else {
				Message.error(error.response.data.message || '请求出错');
			}
		} else if (error.message.includes('timeout')) {
			Message.error('网络请求超时');
		} else if (error.message === 'Network Error') {
			Message.error('网络连接错误');
		} else {
			Message.error(error.message || '未知错误');
		}
		return Promise.reject(error);
	}
);

// 导出 axios 实例
export default service;
