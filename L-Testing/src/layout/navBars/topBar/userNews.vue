<template>
	<div class="layout-navbars-breadcrumb-user-news">
		<div class="head-box">
			<div class="head-box-title">{{ $t('message.user.newTitle') }}</div>
			<div class="head-box-btn" v-if="newsList.length > 0" @click="onAllReadClick">{{ $t('message.user.newBtn') }}</div>
		</div>
		<div class="content-box">
			<template v-if="newsList.length > 0">
				<div class="content-box-item" v-for="(v, k) in newsList" :key="k">
					<div>{{ v.label }}</div>
					<div class="content-box-msg">
						{{ v.value }}
					</div>
					<div class="content-box-time">{{ v.time }}</div>
				</div>
			</template>
			<div class="content-box-empty" v-else>
				<div class="content-box-empty-margin">
					<i class="el-icon-s-promotion"></i>
					<div class="mt15">{{ $t('message.user.newDesc') }}</div>
				</div>
			</div>
		</div>
		<div class="foot-box" @click="onGoToClick" v-if="newsList.length > 0">{{ $t('message.user.newGo') }}</div>
	</div>
</template>

<script>
export default {
	name: 'layoutBreadcrumbUserNews',
	data() {
		return {
			newsList: [],
		};
	},
	methods: {
		// 全部已读点击
		onAllReadClick() {
			this.newsList = [];
		},
		// 前往通知中心点击
		onGoToClick() {
			console.log("按钮被点击了")
		},
	},
	watch: {
		newsList(newVal) {
			this.$emit('news-updated', newVal)
		}
	}
};
</script>

<style scoped lang="scss">
.layout-navbars-breadcrumb-user-news {
	.head-box {
		display: flex;
		border-bottom: 1px solid var(--prev-border-color-lighter);
		box-sizing: border-box;
		color: var(--prev-color-text-primary);
		justify-content: space-between;
		height: 35px;
		align-items: center;
		.head-box-btn {
			color: var(--prev-color-primary);
			font-size: 13px;
			cursor: pointer;
			opacity: 0.8;
			&:hover {
				opacity: 1;
			}
		}
	}
	.content-box {
		font-size: 13px;
		.content-box-item {
			padding-top: 12px;
			&:last-of-type {
				padding-bottom: 12px;
			}
			.content-box-msg {
				color: var(--prev-color-text-secondary);
				margin-top: 5px;
				margin-bottom: 5px;
			}
			.content-box-time {
				color: var(--prev-color-text-secondary);
			}
		}
		.content-box-empty {
			height: 260px;
			display: flex;
			.content-box-empty-margin {
				margin: auto;
				text-align: center;
				i {
					font-size: 60px;
				}
			}
		}
	}
	.foot-box {
		height: 35px;
		color: var(--prev-color-primary);
		font-size: 13px;
		cursor: pointer;
		opacity: 0.8;
		display: flex;
		align-items: center;
		justify-content: center;
		border-top: 1px solid var(--prev-border-color-lighter);
		&:hover {
			opacity: 1;
		}
	}
	::v-deep(.el-empty__description p) {
		font-size: 13px;
	}
}
</style>
