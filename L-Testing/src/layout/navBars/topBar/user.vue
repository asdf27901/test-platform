<template>
	<div class="layout-navbars-breadcrumb-user" :style="{ flex: layoutUserFlexNum }">
		<el-dropdown :show-timeout="70" :hide-timeout="50" trigger="click" @command="onComponentSizeChange">
			<div class="layout-navbars-breadcrumb-user-icon">
				<i class="iconfont icon-ziti" :title="$t('message.user.title0')"></i>
			</div>
			<template #dropdown>
				<el-dropdown-menu>
					<el-dropdown-item command="" :disabled="disabledSize === ''">{{ $t('message.user.dropdownDefault') }}</el-dropdown-item>
					<el-dropdown-item command="medium" :disabled="disabledSize === 'medium'">{{ $t('message.user.dropdownMedium') }}</el-dropdown-item>
					<el-dropdown-item command="small" :disabled="disabledSize === 'small'">{{ $t('message.user.dropdownSmall') }}</el-dropdown-item>
					<el-dropdown-item command="mini" :disabled="disabledSize === 'mini'">{{ $t('message.user.dropdownMini') }}</el-dropdown-item>
				</el-dropdown-menu>
			</template>
		</el-dropdown>
<!--		<el-dropdown :show-timeout="70" :hide-timeout="50" trigger="click" @command="onLanguageChange">-->
<!--			<div class="layout-navbars-breadcrumb-user-icon">-->
<!--				<i class="iconfont" :class="disabledI18n === 'en' ? 'icon-fuhao-yingwen' : 'icon-fuhao-zhongwen'" :title="$t('message.user.title1')"></i>-->
<!--			</div>-->
<!--			<el-dropdown-menu slot="dropdown">-->
<!--				<el-dropdown-item command="zh-cn" :disabled="disabledI18n === 'zh-cn'">简体中文</el-dropdown-item>-->
<!--				<el-dropdown-item command="en" :disabled="disabledI18n === 'en'">English</el-dropdown-item>-->
<!--				<el-dropdown-item command="zh-tw" :disabled="disabledI18n === 'zh-tw'">繁體中文</el-dropdown-item>-->
<!--			</el-dropdown-menu>-->
<!--		</el-dropdown>-->
		<div class="layout-navbars-breadcrumb-user-icon" @click="onSearchClick">
			<i class="el-icon-search" :title="$t('message.user.title2')"></i>
		</div>
		<div class="layout-navbars-breadcrumb-user-icon" @click="onLayoutSetingClick">
			<i class="icon-skin iconfont" :title="$t('message.user.title3')"></i>
		</div>
		<div class="layout-navbars-breadcrumb-user-icon" @clicks.stop="isShowUserNewsPopover = !isShowUserNewsPopover">
			<el-popover placement="bottom" trigger="click" v-model="isShowUserNewsPopover" :width="300" popper-class="el-popover-pupop-user-news">
				<template #reference>
					<el-badge :is-dot="hasNews">
						<i class="el-icon-bell" :title="$t('message.user.title4')"></i>
					</el-badge>
				</template>
				<UserNews v-show="isShowUserNewsPopover" @news-updated="handleNewsUpdated"/>
			</el-popover>
		</div>
		<div class="layout-navbars-breadcrumb-user-icon mr10" @click="onScreenfullClick">
			<i
				class="iconfont"
				:title="isScreenFull ? $t('message.user.title6') : $t('message.user.title5')"
				:class="!isScreenFull ? 'icon-fullscreen' : 'icon-tuichuquanping'"
			></i>
		</div>
		<el-dropdown :show-timeout="70" :hide-timeout="50" @command="onDropdownCommand">
			<span class="layout-navbars-breadcrumb-user-link">
				<img :src="userInfos.avatarUrl ? userInfos.avatarUrl : require('@/assets/kun.jpg')" class="layout-navbars-breadcrumb-user-link-photo mr5" alt=""/>
				{{ userInfos.nickName ? userInfos.nickName : userInfos.username ? userInfos.username : 'undefined' }}
				<i class="el-icon-arrow-down el-icon--right"></i>
			</span>
			<el-dropdown-menu slot="dropdown">
				<el-dropdown-item command="/dataBoard">{{ $t('message.user.dropdown1') }}</el-dropdown-item>
				<el-dropdown-item command="edit">{{ $t('message.user.dropdown2') }}</el-dropdown-item>
				<el-dropdown-item divided command="logOut">{{ $t('message.user.dropdown5') }}</el-dropdown-item>
			</el-dropdown-menu>
		</el-dropdown>
		<Search ref="searchRef" />
		<!-- 4. 新增/编辑用户的抽屉组件 -->
		<user-form-drawer
				:visible.sync="drawerVisible"
				:mode="drawerMode"
				:initial-data="currentUser"
				@success="handleFormSuccess"
		></user-form-drawer>
	</div>
</template>

<script>
import screenfull from 'screenfull';
import { Local } from '@/utils/storage.js';
import UserNews from '@/layout/navBars/topBar/userNews.vue';
import Search from '@/layout/navBars/topBar/search.vue';
import {mapActions, mapState} from "vuex";
import userFormDrawer from "@/views/user/components/userFormDrawer.vue";

export default {
	name: 'layoutBreadcrumbUser',
	components: { UserNews, Search, userFormDrawer },
	data() {
		return {
			isScreenFull: false,
			isShowUserNewsPopover: false,
			disabledI18n: 'zh-cn',
			disabledSize: '',
			hasNews: false,

			// 为抽屉组件管理状态
			drawerVisible: false,
			drawerMode: 'edit', // 'add' 或 'edit'
			currentUser: null,  // 存储当前正在编辑的用户数据
		};
	},
	computed: {
		// 获取用户信息
		...mapState('userInfos', ['userInfos']),
		// 设置弹性盒子布局 flex
		layoutUserFlexNum() {
			let { layout, isClassicSplitMenu } = this.$store.state.themeConfig.themeConfig;
			let num = '';
			if (layout === 'defaults' || (layout === 'classic' && !isClassicSplitMenu) || layout === 'columns') num = 1;
			else num = null;
			return num;
		},
	},
	mounted() {
		if (Local.get('themeConfigPrev')) {
			this.initI18n();
			this.initComponentSize();
		}
	},
	created() {
		this.fetchUserInfos()
	},
	methods: {
		// 搜索点击
		onSearchClick() {
			this.$refs.searchRef.openSearch();
		},
		// 布局配置点击
		onLayoutSetingClick() {
			this.bus.$emit('openSetingsDrawer');
		},
		// 全屏点击
		onScreenfullClick() {
			if (!screenfull.isEnabled) {
				this.$message.warning('暂不不支持全屏');
				return false;
			}
			screenfull.toggle();
			screenfull.on('change', () => {
				this.isScreenFull = screenfull.isFullscreen;
			});
			// 监听菜单 horizontal.vue 滚动条高度更新
			this.bus.$emit('updateElScrollBar');
		},
		// 组件大小改变
		onComponentSizeChange(size) {
			Local.remove('themeConfigPrev');
			this.$store.state.themeConfig.themeConfig.globalComponentSize = size;
			Local.set('themeConfigPrev', this.$store.state.themeConfig.themeConfig);
			this.$ELEMENT.size = size;
			this.initComponentSize();
			window.location.reload();
		},
		// 语言切换
		onLanguageChange(lang) {
			Local.remove('themeConfigPrev');
			this.$store.state.themeConfig.themeConfig.globalI18n = lang;
			Local.set('themeConfigPrev', this.$store.state.themeConfig.themeConfig);
			this.$i18n.locale = lang;
			this.initI18n();
		},
		// 初始化言语国际化
		initI18n() {
			switch (Local.get('themeConfigPrev').globalI18n) {
				case 'zh-cn':
					this.disabledI18n = 'zh-cn';
					break;
				case 'en':
					this.disabledI18n = 'en';
					break;
				case 'zh-tw':
					this.disabledI18n = 'zh-tw';
					break;
			}
		},
		// 初始化全局组件大小
		initComponentSize() {
			switch (Local.get('themeConfigPrev').globalComponentSize) {
				case '':
					this.disabledSize = '';
					break;
				case 'medium':
					this.disabledSize = 'medium';
					break;
				case 'small':
					this.disabledSize = 'small';
					break;
				case 'mini':
					this.disabledSize = 'mini';
					break;
			}
		},
		// `dropdown 下拉菜单` 当前项点击
		onDropdownCommand(path) {
			switch (path) {
				case 'logOut':
					setTimeout(() => {
						this.$msgbox({
							closeOnClickModal: false,
							closeOnPressEscape: false,
							title: this.$t('message.user.logOutTitle'),
							message: this.$t('message.user.logOutMessage'),
							showCancelButton: true,
							confirmButtonText: this.$t('message.user.logOutConfirm'),
							cancelButtonText: this.$t('message.user.logOutCancel'),
							beforeClose: (action, instance, done) => {
								if (action === 'confirm') {
									instance.confirmButtonLoading = true;
									instance.confirmButtonText = this.$t('message.user.logOutExit');
									setTimeout(() => {
										done();
										setTimeout(() => {
											instance.confirmButtonLoading = false;
										}, 300);
									}, 700);
								} else {
									done();
								}
							},
						})
								.then(() => {
									// 清除缓存/token等
									Local.clear();
									// 使用 reload 时，不需要调用 resetRoute() 重置路由
									// window.location.reload();
									this.$router.push({ path: '/login', query: { redirect: this.$route.fullPath } });
								})
								.catch(() => {});
					}, 150);
					break
				case 'edit':
					this.drawerVisible = true
					this.currentUser = this.$store.state.userInfos.userInfos
					break
				default:
					this.$router.push(path);
			}
		},
		handleNewsUpdated(newsList) {
			this.hasNews = newsList.length > 0
		},
		handleFormSuccess() {
			this.fetchUserInfos()
		},
		...mapActions('userInfos', ['fetchUserInfos'])
	},
};
</script>

<style scoped lang="scss">
.layout-navbars-breadcrumb-user {
	display: flex;
	align-items: center;
	justify-content: flex-end;
	&-link {
		height: 100%;
		display: flex;
		align-items: center;
		white-space: nowrap;
		&-photo {
			width: 25px;
			height: 25px;
			border-radius: 100%;
		}
	}
	&-icon {
		padding: 0 10px;
		cursor: pointer;
		color: var(--prev-bg-topBarColor);
		height: 50px;
		line-height: 50px;
		display: flex;
		align-items: center;
		&:hover {
			background: var(--prev-color-hover);
			i {
				display: inline-block;
				animation: logoAnimation 0.3s ease-in-out;
			}
		}
	}
	& ::v-deep .el-dropdown {
		color: var(--prev-bg-topBarColor);
	}
	& ::v-deep .el-badge {
		height: 40px;
		line-height: 40px;
		display: flex;
		align-items: center;
	}
	& ::v-deep .el-badge__content.is-fixed {
		top: 12px;
	}
}
</style>
