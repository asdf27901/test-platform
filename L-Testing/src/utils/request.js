import axios from 'axios';
import { Message } from 'element-ui';
import { Local } from '@/utils/storage';
import router from "@/router";
import qs from 'qs'
import webSocketService from "@/utils/webSocketService";

// 创建 axios 实例
const service = axios.create({
	baseURL: process.env.VUE_APP_BASE_API,
	timeout: 50000,
	// headers: { 'Content-Type': 'application/json' },
});

// 用于防止重复弹窗和重定向的标志
let isRedirecting = false;

// 使用 Map 存储每个请求的标识和取消函数
const pendingRequests = new Map();

// 生成每个请求唯一的键
function getPendingKey(config) {
	const { method, url, params, data } = config;
	return [method, url, qs.stringify(params), qs.stringify(data)].join('&');
}

// 添加请求到 pendingRequests
function addPending(config) {
	const key = getPendingKey(config);
	config.cancelToken = config.cancelToken || new axios.CancelToken((cancel) => {
		if (!pendingRequests.has(key)) {
			pendingRequests.set(key, cancel);
		}
	});
}

// 从 pendingRequests 中移除请求
function removePending(config) {
	const key = getPendingKey(config);
	if (pendingRequests.has(key)) {
		pendingRequests.delete(key);
	}
}

// 清空所有 pending 的请求
export function clearAllPending() {
	for (const [key, cancel] of pendingRequests) {
		cancel();
	}
	pendingRequests.clear();
}

// 添加请求拦截器
service.interceptors.request.use(
	(config) => {
		addPending(config)
		// 在发送请求之前做些什么 token
		const token = Local.get('token')
		if (token) {
			config.headers['token'] = token;
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
		removePending(response.config)
		// 对响应数据做点什么
		const res = response.data;
		if (res.code && res.code !== 200) {
			if ((res.code === 601 || res.code === 602) && !isRedirecting) {
				isRedirecting = true
				clearAllPending()
				Local.remove('token')
				webSocketService.disconnect()
				const redirect = router.currentRoute.fullPath
				router.push({
					path: '/login',
					query: {
						redirect
					}
				}).finally(() => {isRedirecting = false})
			}
			return Promise.reject(res);
		} else {
			return res
		}
	},
	(error) => {
		if (axios.isCancel(error)) {
			if (error.config) {
				removePending(error.config);
			}
			return new Promise(() => {});
		}
		if (error.config) {
			removePending(error.config)
		}

		// 对响应错误做点什么
		if (error.response) {
			// HTTP状态码非2xx的情况
			const status = error.response.status;
			const message = error.response.data.message || '请求出错';
			switch (status) {
				case 401:
					Message.error('未授权，请重新登录');
					break;
				case 403:
					Message.error('拒绝访问');
					break;
				case 404:
					Message.error('请求资源不存在');
					break;
				case 500:
				case 502:
				case 503:
					Message.error('服务器错误，请稍后重试');
					break;
				default:
					Message.error(message);
					break;
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
