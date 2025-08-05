import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { Local } from '@/utils/storage'; // 引入你的本地存储工具
import { Notification } from 'element-ui'
import router from "@/router";
import {messageApis} from "@/api/message";

class WebSocketService {
    constructor() {
        this.stompClient = null;
        this.subscriptions = {}; // 存储所有订阅，方便统一取消
        this.isConnected = false;
        this.isConnecting = false;
        this.store = null; // 我们会在这里注入 Vuex store

        this.reconnectAttempts = 0; // 当前重连尝试次数
        this.maxReconnectAttempts = 3; // 最大重连次数
        this.reconnectTimeout = null;
    }

    /**
     * 初始化服务并注入 Vuex store
     * @param {object} store - Vuex store 实例
     */
    init(store) {
        this.store = store;
    }

    /**
     * 连接到 WebSocket 服务器
     */
    connect(isReconnect = false) {
        if (this.isConnected || this.isConnecting) {
            return;
        }

        this.isConnecting = true;

        // 如果不是重连，则重置重连计数器
        if (!isReconnect) {
            this.reconnectAttempts = 0;
        }

        const token = Local.get('token');
        if (!token) {
            return;
        }

        const socket = new SockJS('http://localhost:8080/ws-notify');
        this.stompClient = Stomp.over(socket);

        // 禁止 stomp.js 在内部打印 debug 信息
        this.stompClient.debug = null;

        const headers = { 'token': token };

        this.stompClient.connect(headers,
            () => {
                // --- 连接成功 ---
                this.isConnected = true;
                this.isConnecting = false;
                this.reconnectAttempts = 0; // 连接成功，重置计数器

                if (this.reconnectTimeout) {
                    clearTimeout(this.reconnectTimeout);
                    this.reconnectTimeout = null;
                }

                this.subscribeToGlobalTopics();
            },
            () => {
                // --- 连接失败 ---
                this.isConnected = false;
                this.isConnecting = false;

                // --- 执行有限重连逻辑 ---
                this.handleReconnect();
            }
        );
    }

    /**
     * 处理重连逻辑
     */
    handleReconnect() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;

            this.reconnectTimeout = setTimeout(() => {
                this.connect(true);
            }, 5000);
        } else {
            this.isConnecting = false
        }
    }

    /**
     * 订阅所有需要全局监听的主题
     */
    subscribeToGlobalTopics() {
        const notificationSubscription = this.stompClient.subscribe('/user/queue/notifications', (message) => {
            const payload = JSON.parse(message.body);

            // 使用 Vuex action 来处理收到的消息
            if (this.store) {
                this.store.dispatch('message/addMessage', payload)
            }

            Notification({
                title: '您有一条新的消息~',
                message: payload.content,
                type: payload.type,
                showClose: false,
                onClick: () => {
                    messageApis.markMessageAsRead({ids: payload.id})
                    this.store.dispatch('message/removeUnReadMessage', payload.id)
                    router.push(payload.path)
                }
            })
        });

        // 将订阅保存起来
        this.subscriptions['notifications'] = notificationSubscription;
    }

    /**
     * 断开连接
     */
    disconnect() {
        this.isConnecting = false;
        if (this.reconnectTimeout) {
            clearTimeout(this.reconnectTimeout);
            this.reconnectTimeout = null;
        }
        this.reconnectAttempts = 0; // 重置计数器
        if (this.stompClient !== null && this.isConnected) {
            // 取消所有订阅
            Object.values(this.subscriptions).forEach(sub => sub.unsubscribe());
            this.subscriptions = {};

            this.stompClient.disconnect(() => {
                this.isConnected = false;
            });
        }
    }
}

// 导出一个单例，确保整个应用共享同一个 WebSocket 连接
export default new WebSocketService();
