<template>
	<el-menu
		router
		background-color="transparent"
		:default-active="defaultActive"
		:collapse="setIsCollapse"
		:unique-opened="getThemeConfig.isUniqueOpened"
		:collapse-transition="false"
	>
		<template v-for="val in menuList">
			<el-submenu :index="val.path" v-if="val.children && val.children.length > 0" :key="val.path">
				<template slot="title">
					<i :class="val.meta.icon ? val.meta.icon : ''"></i>
					<span>{{ $t(val.meta.title) }}</span>
				</template>
				<SubItem :chil="val.children" />
			</el-submenu>
			<template v-else>
				<el-menu-item :index="val.path" :key="val.path">
					<i :class="val.meta.icon ? val.meta.icon : ''"></i>
					<template slot="title" v-if="!val.meta.isLink || (val.meta.isLink && val.meta.isIframe)">
						<span>{{ $t(val.meta.title) }}</span>
					</template>
					<template slot="title" v-else>
						<a :href="val.meta.isLink" target="_blank">{{ $t(val.meta.title) }}</a>
					</template>
				</el-menu-item>
			</template>
		</template>
	</el-menu>
</template>

<script>
import SubItem from '@/layout/navMenu/subItem.vue';
export default {
	name: 'navMenuVertical',
	components: { SubItem },
	props: {
		menuList: {
			type: Array,
			default() {
				return [];
			},
		},
	},
	data() {
		return {
			// defaultActive: this.$route.path,
		};
	},
	computed: {
		// 获取布局配置信息
		getThemeConfig() {
			return this.$store.state.themeConfig.themeConfig;
		},
		// 设置左侧菜单是否展开/收起
		setIsCollapse() {
			return document.body.clientWidth < 1000 ? false : this.$store.state.themeConfig.themeConfig.isCollapse;
		},
		// 新增：智能计算当前激活的菜单
		defaultActive() {
			const { meta, path } = this.$route;
			// 如果路由元信息中配置了 activeMenu，则使用它作为高亮路径
			if (meta.activeMenu) {
				return meta.activeMenu;
			}
			// 否则，直接使用当前路由路径
			return path;
		},
	},
	watch: {
		// 监听路由的变化
		$route: {
			// handler(to) {
			// 	this.defaultActive = to.path;
			// 	const clientWidth = document.body.clientWidth;
			// 	if (clientWidth < 1000) this.$store.state.themeConfig.themeConfig.isCollapse = false;
			// },
			handler() {
				const clientWidth = document.body.clientWidth;
				if (clientWidth < 1000) this.$store.state.themeConfig.themeConfig.isCollapse = false;
			},
			deep: true,
		},
	},
};
</script>
