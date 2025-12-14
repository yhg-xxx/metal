import {createRouter, createWebHashHistory} from "vue-router";

const router = createRouter({
        history: createWebHashHistory(import.meta.env.BASE_URL),
        routes: [
            {
                path: '/',
                name: 'login',
                component: () => import('@/components/Login.vue'),
            },
            {
                path: '/view',
                name: 'view',
                component: () => import('@/components/IndexView.vue'),
                redirect: '/view/profile',
                children: [
                    {
                        path: 'profile',
                        name: 'profile',
                        component: () => import('@/components/Profile.vue') // 个人信息页面组件
                    },
                    
            {                path: 'messages',
                name: 'messages',
                component: () => import('@/components/MessageList.vue'),
                children: [
                    {                        path: 'chat-detail',
                        name: 'chatDetail',
                        component: () => import('@/components/ChatDetail.vue')
                    }
                ]
            }
                ]
            }
        ]
    }
)
export default router;