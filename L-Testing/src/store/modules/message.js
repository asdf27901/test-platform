import {messageApis} from "@/api/message";
import {Message} from "element-ui";

const messageModule = {
    namespaced: true,
    state: {
        recentMessageList: [],
        unReadMessageList: [],
    },
    mutations: {
        // 设置路由，菜单中使用到
        getRecentMessageList(state, data) {
            state.recentMessageList = data;
        },
        appendRecentMessage(state, data) {
            state.recentMessageList.unshift(data)
        },
        appendUnReadMessage(state, data) {
            state.unReadMessageList.unshift(data)
        },
        popUnReadMessage(state, id) {
            state.unReadMessageList = state.unReadMessageList.filter(msg => msg.id !== id)
        },
        removeAllUnReadMessage(state) {
            state.unReadMessageList = []
        }
    },
    actions: {
        // 设置路由，菜单中使用到
        setRecentMessageList({ commit }, data) {
            commit('getRecentMessageList', data);
        },
        addMessage({ commit }, data) {
            commit('appendUnReadMessage', data)
            commit('appendRecentMessage', data)
        },
        async fetchAllUnReadMessageList({ commit }, params) {
            try {
                const { data } = await messageApis.getAllUnReadMessage(params)
                data.forEach(d => {
                    commit('appendUnReadMessage', d)
                })
                return data
            } catch (e) {
                Message.error(e.message)
            }
        },
        removeUnReadMessage({ commit }, id) {
            commit('popUnReadMessage', id)
        },
        removeAllUnReadMessage({ commit }) {
            commit('removeAllUnReadMessage')
        },
    },
};

export default messageModule;
