export const routerMap = [
    {
        path: '/',
        name: 'layout',
        component: () => import('@/layout/index.vue'),
        redirect: '/dataBoard',
        children: [
            {
                path: '/dataBoard',
                name: 'DataBoard',
                component: () => import('@/views/databoard'),
                meta: {
                    title: 'message.router.dataBoard',
                    icon: 'fa fa-database',
                    isLink: "",
                    isHide: false,
                    isKeepAlive: true,
                    isAffix: true,
                    isIframe: false,
                }
            },
            {
                path: '/user',
                name: 'UserManagement',
                component: () => import('@/views/user'),
                meta: {
                    title: 'message.router.user',
                    icon: 'fa fa-users',
                    isLink: "",
                    isHide: false,
                    isKeepAlive: true,
                    isAffix: false,
                    isIframe: false,
                }
            }
        ]
    },
    {
        path: '/login',
        name: 'login',
        component: () => import('@/views/login'),
        meta: {
            title: 'message.router.signIn',
        },
    },
    {
        path: '*',
        name: 'notFound',
        component: () => import('@/views/error/404.vue'),
        meta: {
            title: 'message.router.notFound',
        },
    }
]
