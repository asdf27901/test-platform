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
                    icon: 'fa fa-bar-chart-o',
                    isLink: "",
                    isHide: false,
                    isKeepAlive: false,
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
            },
            {
                path: "/interfaces-test",
                name: "interfaces-test",
                component: () => import('@/layout/routerView/parent.vue'),
                meta: {
                    title: "message.router.interfacesTest",
                    isLink: "",
                    isHide: false,
                    isKeepAlive: false,
                    isAffix: false,
                    isIframe: false,
                    icon: "iconfont icon-caidan"
                },
                children: [
                    {
                        path: "/interfaces-test/interfaces",
                        name: "InterfaceList",
                        component: () => import('@/views/interface-test/interfaces.vue'),
                        meta: {
                            title: "message.router.interfaces",
                            isLink: "",
                            isHide: false,
                            isKeepAlive: true,
                            isAffix: false,
                            isIframe: false,
                            icon: "fa fa-plug"
                        },
                        props: true
                    },
                    {
                        path: "/interfaces-test/testcaseList",
                        name: "InterfaceTestcaseList",
                        component: () => import('@/views/interface-test/interfaceTestcaseList.vue'),
                        meta: {
                            title: "message.router.interfaceTestcaseList",
                            isLink: "",
                            isHide: false,
                            isKeepAlive: true,
                            isAffix: false,
                            isIframe: false,
                            icon: 'fa fa-hand-peace-o'
                        },
                    },
                    {
                        path: "/interfaces-test/testcaseList/addTestcase",
                        name: "InterfaceTestcaseEditor",
                        component: () => import('@/views/interface-test/interfaceTestcaseEditor.vue'),
                        props: {
                           mode: 'add'
                        },
                        meta: {
                            title: "message.router.addInterfaceTestcase",
                            isLink: "",
                            isHide: true,
                            isKeepAlive: true,
                            isAffix: false,
                            isIframe: false,
                            isHideInTagsView: false,
                            icon: 'fa fa-plus',
                            activeMenu: "/interfaces-test/testcaseList"
                        }
                    },
                    {
                        path: "/interfaces-test/testcaseList/editTestcase",
                        name: "InterfaceTestcaseEditor",
                        component: () => import('@/views/interface-test/interfaceTestcaseEditor.vue'),
                        props: {
                            mode: 'edit'
                        },
                        meta: {
                            title: "message.router.editInterfaceTestcase",
                            isLink: "",
                            isHide: true,
                            isKeepAlive: true,
                            isAffix: false,
                            isIframe: false,
                            isHideInTagsView: false,
                            icon: 'fa fa-edit',
                            activeMenu: "/interfaces-test/testcaseList"
                        }
                    },
                    {
                        path: "/interfaces-test/chainRequestList",
                        name: "ChainRequestList",
                        component: () => import('@/views/interface-test/chainRequestList.vue'),
                        meta: {
                            title: "message.router.chainRequestList",
                            isLink: "",
                            isHide: false,
                            isKeepAlive: true,
                            isAffix: false,
                            isIframe: false,
                            isHideInTagsView: false,
                            icon: 'fa fa-chain',
                        }
                    },
                    {
                        path: "/interfaces-test/apiRequestLog",
                        name: "ApiRequestLogList",
                        component: () => import('@/views/interface-test/apiRequestLogList.vue'),
                        meta: {
                            title: "message.router.apiRequestLog",
                            isLink: "",
                            isHide: false,
                            isKeepAlive: true,
                            isAffix: false,
                            isIframe: false,
                            isHideInTagsView: false,
                            icon: 'fa fa-pencil-square-o',
                        }
                    },
                    {
                        path: "/interfaces-test/apiRequestLog/apiRequestLogDetail",
                        name: 'ApiRequestLogDetail',
                        component: () => import('@/views/interface-test/apiRequestLogDetail.vue'),
                        meta: {
                            title: "message.router.apiRequestLogDetail",
                            isLink: "",
                            isHide: true,
                            isKeepAlive: true,
                            isAffix: false,
                            isIframe: false,
                            isHideInTagsView: false,
                            icon: 'fa fa-newspaper-o',
                            activeMenu: "/interfaces-test/apiRequestLog"
                        }
                    }
                ]
            },
            {
                path: "/config",
                name: "config",
                component: () => import('@/layout/routerView/parent.vue'),
                meta: {
                    title: "message.router.config",
                    isLink: "",
                    isHide: false,
                    isKeepAlive: false,
                    isAffix: false,
                    isIframe: false,
                    icon: "fa fa-gear"
                },
                children: [
                    {
                        path: "/config/environmentVariable",
                        name: "EnvironmentVariable",
                        component: () => import('@/views/config/environmentVariable.vue'),
                        meta: {
                            title: "message.router.environmentVariable",
                            isLink: "",
                            isHide: false,
                            isKeepAlive: false,
                            isAffix: false,
                            isIframe: false,
                            icon: "fa fa-vimeo"
                        },
                    }
                ]
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
