import Vue from 'vue';
import store from '../store';
import VueRouter from 'vue-router';
import NProgress from 'nprogress';
import 'nprogress/nprogress.css';
import { Local } from '@/utils/storage';
import { PrevLoading } from '@/utils/loading.js';
import { routerMap } from "@/router/routerMap";

// 解决 `element ui` 导航栏重复点菜单报错问题
const originalPush = VueRouter.prototype.push;
VueRouter.prototype.push = function push(location) {
	return originalPush.call(this, location).catch((err) => err);
};

// 安装 VueRouter 插件
Vue.use(VueRouter);

// 加载静态路由
const createRouter = () =>
	new VueRouter({
		mode: 'history',
		routes: routerMap,
	});

// 创建路由
const router = createRouter();

// 加载 loading
PrevLoading.start();

// 多级嵌套数组处理成一维数组
export function formatFlatteningRoutes(arr) {
	if (arr.length <= 0) return false;
	for (let i = 0; i < arr.length; i++) {
		if (arr[i].children) {
			arr = arr.slice(0, i + 1).concat(arr[i].children, arr.slice(i + 1));
		}
	}
	return arr;
}

// 处理 tagsViewList 数据，默认路由全部缓存
// isKeepAlive 处理 `name` 值，进行路由缓存
export function formatTwoStageRoutes(arr) {
	if (arr.length <= 0) return false;
	const newArr = [];
	const cacheList = [];
	arr.forEach((v) => {
		if (!v.meta || !v.meta.isHideInTagsView) {
			newArr.push({ ...v });
		}
		if (v.meta && v.meta.isKeepAlive) {
			cacheList.push(v.name);
		}
		// newArr.push({ ...v });
		// cacheList.push(v.name);
		store.dispatch('keepAliveNames/setCacheKeepAlive', cacheList);
	});
	return newArr;
}

(() => {
    routerMap.forEach(route => {
        if (Object.keys(route).includes('children')) {
            // 将children中所有的路由全部存入routesList中
            const routes = []
            route.children.forEach(r => {
                routes.push(r)
            })
            store.dispatch('routesList/setRoutesList', routes);
            store.dispatch('tagsViewRoutes/setTagsViewRoutes', formatTwoStageRoutes(formatFlatteningRoutes(routes)));
        }
    })
})();

// 递归处理多余的 layout : <router-view>，让需要访问的组件保持在第一层 layout 层。
// 因为 `keep-alive` 只能缓存二级路由
// 默认初始化时就执行
export function keepAliveSplice(to) {
	if (to.matched && to.matched.length > 2) {
		to.matched.map((v, k) => {
			if (v.components.default instanceof Function) {
				v.components.default().then((components) => {
					if (components.default.name === 'parent') {
						to.matched.splice(k, 1);
						router.push({ path: to.path, query: to.query });
						keepAliveSplice(to);
					}
				});
			} else {
				if (v.components.default.name === 'parent') {
					to.matched.splice(k, 1);
					keepAliveSplice(to);
				}
			}
		});
	}
}

// 重置路由
export function resetRouter() {
	router.matcher = createRouter().matcher;
}

// 延迟关闭进度条
export function delayNProgressDone(time = 300) {
	setTimeout(() => {
		NProgress.done();
	}, time);
}

// 路由加载前
router.beforeEach((to, from, next) => {
	keepAliveSplice(to);
	NProgress.configure({ showSpinner: false });

	const token = Local.get('token');
	const isLoginPage = to.path === '/login';

	// 只有当目标页面有title且不是/login时，才显示进度条
	if (to.meta.title && !isLoginPage) {
		NProgress.start();
	}

	if (isLoginPage) {
		// 如果是登录页，且无token，正常进入登录页
		if (!token) {
			next();
			delayNProgressDone();
			return;
		} else {
			// 已登录，访问登录页，跳回首页或其他默认页面
			next({ path: '/' });
			delayNProgressDone();
			return;
		}
	}
	// 需要登录的页面且无token，强制跳转登录页
	if (!token) {
		next({ path: '/login' });
		delayNProgressDone();
		return;
	}
	// 其他正常页面直接放行
	next();
	delayNProgressDone();
});

// 路由加载后
router.afterEach(() => {
	PrevLoading.done();
	delayNProgressDone();
});

// 导出路由
export default router;
