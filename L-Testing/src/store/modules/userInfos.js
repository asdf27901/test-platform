import {userApis} from "@/api/user";
import {Message} from "element-ui";

const userInfosModule = {
	namespaced: true,
	state: {
		userInfos: {},
	},
	mutations: {
		// 设置用户信息
		setUserInfos(state, data) {
			state.userInfos = data;
		},
	},
	actions: {
		// 设置用户信息
		async fetchUserInfos({ commit }) {
			try {
				const {data} = await userApis.getUserInfo()
				if (data) {
					commit('setUserInfos', data)
				}
			}catch (error) {
				if (error.code) {
					Message.error(error.message)
				}
			}
		},
	},
};

export default userInfosModule;
