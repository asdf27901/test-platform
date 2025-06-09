export const routerMap = [
    {
        path: '/login',
        name: 'login',
        component: () => import('@/views/login'),
        meta: {
            title: '登录',
        },
    },
    {
        path: '*',
        name: 'notFound',
        component: () => import('@/views/error/404.vue'),
        meta: {
            title: 'message.staticRoutes.notFound',
        },
    }
]
