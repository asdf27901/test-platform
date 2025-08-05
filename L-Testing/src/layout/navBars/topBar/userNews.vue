<template>
	<div class="layout-navbars-breadcrumb-user-news">
		<div class="head-box">
			<div class="head-box-title">{{ $t('message.user.newTitle') }}</div>
			<div class="head-box-btn" v-if="newsList.length > 0" @click="onAllReadClick">{{ $t('message.user.newBtn') }}</div>
		</div>
		<div class="content-box" ref="contentBoxRef" @scroll="handleScroll">
			<template v-if="newsList.length > 0">
				<div class="content-box-item" v-for="(v, k) in newsList" :key="k" @click="onSingleReadClick(v)">
					<div>未读消息 - {{ v.createdTime }}</div>
					<div class="content-box-msg">
						{{ v.content }}
					</div>
					<div class="content-box-time">{{ v.createdTime }}</div>
				</div>
			</template>
			<div class="content-box-empty" v-if="newsList.length === 0 && finished">
				<div class="content-box-empty-margin">
					<i class="el-icon-s-promotion"></i>
					<div class="mt15">{{ $t('message.user.newDesc') }}</div>
				</div>
			</div>

			<!-- 新增：加载和完成状态的提示 -->
			<div class="load-more-status">
				<p v-if="loading">加载中...</p>
				<p v-if="finished && newsList.length > 0">没有更多了</p>
			</div>
		</div>
	</div>
</template>

<script>
import {mapActions} from "vuex";
import {messageApis} from "@/api/message";
import {Message} from "element-ui";

export default {
	name: 'layoutBreadcrumbUserNews',
	data() {
		return {
			// newsList: [],
			pagination: {
				page: 1,
				pageSize: 10, // 每次加载10条
			},
			loading: false,
			finished: false,
		};
	},
	computed: {
		newsList() {
			return this.$store.state.message.unReadMessageList
		}
	},
	created() {
		this.loadMore();
	},
	methods: {
		async loadMore() {
			// 如果正在加载或已经全部加载完毕，则不执行
			if (this.loading || this.finished) {
				return;
			}
			this.loading = true;

			try {
				const newMessage = await this.fetchAllUnReadMessageList(this.pagination);

				if (newMessage && newMessage.length > 0) {
					// 页码+1
					this.pagination.page++;
				}

				// 如果返回的数据量小于每页数量，说明已经没有更多数据了
				if (!newMessage || newMessage.length < this.pagination.pageSize) {
					this.finished = true;
				}
			} catch (e) {
				// 加载失败也认为完成，防止无限重试
				this.finished = true;
			} finally {
				this.loading = false;
			}
		},
		// 全部已读点击
		onAllReadClick() {
			const ids = this.$store.state.message.unReadMessageList.map(message => message.id)
			this.removeAllUnReadMessage()
			try {
				messageApis.markMessageAsRead({ids: ids.join(',')})
			} catch (e) {
				if (e.code) {
					Message.error(e.message)
				}
			}
		},
		onSingleReadClick(v) {
			this.removeUnReadMessage(v.id)
			this.$router.push(v.path)
			try {
				messageApis.markMessageAsRead({ids: v.id})
			} catch (e) {
				if (e.code) {
					Message.error(e.message)
				}
			}
		},
		handleScroll() {
			const { scrollTop, clientHeight, scrollHeight } = this.$refs.contentBoxRef;
			// 滚动到距离底部 10px 时，触发加载更多
			if (scrollTop + clientHeight >= scrollHeight - 10) {
				this.loadMore();
			}
		},
		...mapActions('message', ['fetchAllUnReadMessageList', 'removeAllUnReadMessage', 'removeUnReadMessage'])
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
		padding: 0 15px; // 增加左右内边距
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
		// 新增：固定高度和滚动条
		max-height: 260px;
		overflow-y: auto;

		.content-box-item {
			padding: 12px 15px; // 增加左右内边距
			cursor: pointer; // 增加可点击手势
			border-bottom: 1px solid var(--prev-border-color-lighter); // 增加分割线

			&:hover {
				background-color: var(--prev-bg-color-hover);
			}

			&:last-of-type {
				border-bottom: none;
			}
			.content-box-msg {
				color: var(--prev-color-text-secondary);
				margin-top: 5px;
				margin-bottom: 5px;
			}
			.content-box-time {
				color: var(--prev-color-text-secondary);
				font-size: 12px; // 时间字体可以小一点
			}
		}
		.content-box-empty {
			// 修改：高度由内容撑开，不再固定
			// height: 260px;
			padding: 60px 0; // 增加上下内边距
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

	// 新增：加载状态样式
	.load-more-status {
		text-align: center;
		color: var(--prev-color-text-secondary);
		font-size: 12px;
		padding: 10px 0;
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
