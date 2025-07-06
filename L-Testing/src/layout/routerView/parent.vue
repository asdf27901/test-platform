<template>
	<div class="h100">
		<transition :name="setTransitionName" mode="out-in">
			<keep-alive :include="keepAliveNameList">
				<router-view :key="refreshRouterViewKey" />
			</keep-alive>
		</transition>
	</div>
</template>

<script>
export default {
	name: 'parent',
	data() {
		return {
			refreshRouterViewKey: null,
			keepAliveNameList: [],
			keepAliveNameNewList: [],
		};
	},
	created() {
		// 页面加载前，处理缓存，页面刷新时路由缓存处理
		this.keepAliveNameList = this.getKeepAliveNames();
		this.bus.$on('onTagsViewRefreshRouterView', (path) => {
			if (this.$route.path !== path) return false;
			this.keepAliveNameList = this.getKeepAliveNames().filter((name) => this.$route.name !== name);
			this.refreshRouterViewKey = this.$route.path;
			this.$nextTick(() => {
				// 这里设置为将refreshRouterViewKey为null是为了更新绑定在组件的key
				// 如果keep-alive没有包含当前组件，那么当key发生变更时，就会销毁重新创建组件
				// 如果keep-alive包含当前组件，那么key发生变更时，也只是触发组件的activated方法
				this.refreshRouterViewKey = null;
				this.keepAliveNameList = this.getKeepAliveNames();
			});
		});
	},
	computed: {
		// 设置主界面切换动画
		setTransitionName() {
			return this.$store.state.themeConfig.themeConfig.animation;
		},
	},
	methods: {
		// 获取路由缓存列表（name），默认路由全部缓存
		getKeepAliveNames() {
			return this.$store.state.keepAliveNames.keepAliveNames;
		},
	},
};
</script>
