<template>
	<div class="home">
		<!-- 用户信息 -->
		<el-card shadow="hover" style="margin-bottom: 10px" v-loading="loading">
			<div slot="header">
				<span>数据概览</span>
			</div>
			<el-row :gutter="20">
				<el-col :span="6" v-for="item in dataOverviewList" :key="item.id">
					<div class="stat-card">
						<i :class="item.icon" class="stat-card-icon" :style="{ color: item.color }"></i>
						<div class="stat-card-content">
							<div class="stat-card-title">{{ item.title }}</div>
							<div :id="item.id" class="stat-card-value">
								{{ item.value }}
							</div>
						</div>
					</div>
				</el-col>
			</el-row>
		</el-card>
		<el-row :gutter="15">
			<el-col :md="24" :lg="16" :xl="16" class="mb15">
				<el-card shadow="hover">
					<div slot="header">
						<span>我的工作台</span>
					</div>
					<div class="user-item">
						<div class="user-item-left">
							<img :src="getUserInfos.avatarUrl" />
						</div>
						<div class="user-item-right overflow">
							<el-row>
								<el-col :span="24" class="right-title mb15 one-text-overflow"
									>{{ currentTime }}，{{ getUserInfos.nickName }}，{{ dailyMessage }}
								</el-col>
								<el-col :span="24">
									<el-col :xs="5" :sm="5" :md="5" class="right-l-v">
										<div class="right-label">昵称：</div>
										<div class="right-value">{{getUserInfos.nickName}}</div>
									</el-col>
									<el-col :xs="12" :sm="12" :md="12" class="right-l-v">
										<div class="right-label one-text-overflow">时间：</div>
										<div class="right-value one-text-overflow">{{ time.txt }}</div>
									</el-col>
								</el-col>
							</el-row>
						</div>
					</div>
				</el-card>
			</el-col>
			<el-col :md="24" :lg="8" :xl="8" class="mb15">
				<el-card shadow="hover">
					<div slot="header">
						<span>消息通知</span>
					</div>
					<div class="info">
						<Scroll :data="newsInfoLis" class="info-scroll" :class-option="optionSingleHeight">
							<ul class="info-ul">
								<li v-for="(v, k) in newsInfoLis" :key="k" class="info-item" @click="onNewsInfoListClick(v)">
									<div class="info-item-left" v-text="v.content"></div>
									<div class="info-item-right" v-text="v.createdTime"></div>
								</li>
							</ul>
						</Scroll>
					</div>
				</el-card>
			</el-col>
		</el-row>

		<!-- 推荐 -->
		<el-card shadow="hover">
			<div slot="header">
				<span>快捷入口</span>
			</div>
			<el-row :gutter="15" class="home-recommend-row">
				<el-col :sm="24" :md="12" :lg="6" :xl="6" v-for="(v, k) in recommendList" :key="k">
					<div class="home-recommend" :style="{ 'background-color': v.bg }" @click="$router.push(v.path)">
						<i :class="v.icon" :style="{ color: v.iconColor }"></i>
						<div class="home-recommend-auto">
							<div>{{ v.title }}</div>
							<div class="home-recommend-msg">{{ v.msg }}</div>
						</div>
					</div>
				</el-col>
			</el-row>
		</el-card>

		<el-row :gutter="15" class="mt15">
			<el-col :md="24" :lg="8" :xl="8" class="mb15">
				<el-card shadow="hover">
					<div slot="header">
						<span>请求结果占比</span>
					</div>
					<div class="charts">
						<div class="charts-right">
							<div ref="requestRatios" class="h100"></div>
						</div>
					</div>
				</el-card>
			</el-col>
			<el-col :md="24" :lg="16" :xl="16" class="mb15">
				<el-card shadow="hover">
					<div slot="header">
						<span>创建用例总数前五</span>
					</div>
					<div class="charts">
						<div class="charts-left mr7">
							<div ref="testCaseCreationTop5" class="h100"></div>
						</div>
					</div>
				</el-card>
			</el-col>
		</el-row>

		<el-row :gutter="15">
			<el-col :md="24" :lg="24" :xl="24" class="home-lg">
				<el-card shadow="hover">
					<div slot="header">
						<span>最近12天执行用例情况</span>
					</div>
					<div class="charts">
						<div class="charts-left mr7">
							<div ref="requestDailyDetail" class="h100"></div>
						</div>
					</div>
				</el-card>
			</el-col>
		</el-row>
	</div>
</template>

<script>
import * as echarts from 'echarts';
import Scroll from 'vue-seamless-scroll';
import { CountUp } from 'countup.js';
import { formatAxis, formatDate } from '@/utils/formatTime';
import { recommendList, dailyMessage } from './mock';
import {dataBoardApis} from "@/api/dataBoard";
import {Message} from "element-ui";
import {messageApis} from "@/api/message";
import {mapActions} from "vuex";
export default {
	name: 'DataBoard',
	components: { Scroll },
	data() {
		return {
			loading: false,
			recommendList,
			dataOverviewList: [
				{ id: 'titleNum1', title: '总请求次数', value: 0, icon: 'el-icon-data-line', color: '#409EFF' },
				{ id: 'titleNum2', title: '用户数量', value: 0, icon: 'el-icon-user-solid', color: '#67C23A' },
				{ id: 'titleNum3', title: '用例总数', value: 0, icon: 'el-icon-tickets', color: '#E6A23C' },
				{ id: 'titleNum4', title: '接口总数', value: 0, icon: 'el-icon-link', color: '#F56C6C' },
			],
			requestRatioData: [
				{name: "成功", value: 0},
				{name: "异常", value: 0},
				{name: "失败", value: 0}
			],
			testCaseCreationTop5: [],
			requestDailyDetails: [],
			time: {
				txt: '',
				fun: null,
			},
			dailyMessage: {},
			charts: {
				theme: '',
				bgColor: '',
			},
			global: {
				homeChartOne: null,
				homeChartTwo: null,
				homeChartThree: null,
				dispose: [null, '', undefined],
			},
		};
	},
	created() {
		this.fetchDataBoardData()
		this.initTime()
		this.initDailyMessage();
	},
	computed: {
		currentTime() {
			return formatAxis(new Date());
		},
		optionSingleHeight() {
			return {
				singleHeight: 28,
				limitMoveNum: 8,
				waitTime: 2000,
			};
		},
		getUserInfos() {
			return this.$store.state.userInfos.userInfos;
		},
		newsInfoLis() {
			return this.$store.state.message.recentMessageList
		}
	},
	methods: {
		async fetchDataBoardData() {
			this.loading = true
			try {
				const [{data: charData}, {data: messageList}] = await Promise.all([
					dataBoardApis.getData(),
					messageApis.getRecentMessageList()
				])
				this.setRecentMessageList(messageList)
				const basicDataKeys = Object.keys(charData.basicData)
				if (basicDataKeys.length > 0) {
					for (let i = 0; i < this.dataOverviewList.length; i++) {
						this.dataOverviewList[i].value = charData.basicData[basicDataKeys[i]] || 0
					}
				}
				this.requestRatioData = charData.requestRatios
				this.testCaseCreationTop5 = charData.testCaseCreationTop5
				this.requestDailyDetails = charData.dailyRequestDetails
			} catch (e) {
				if (e.code) {
					Message.error(e.message)
				}
			} finally {
				this.loading = false
				this.initNumCountUp()
				this.initRequestRatios();
				this.initTestCaseCreationTop5();
				this.initRequestDailyDetail()
			}
		},
		// 初始化数字滚动
		initNumCountUp() {
			this.$nextTick(() => {
				this.dataOverviewList.forEach(item => {
					const endVal = item.value
					new CountUp(item.id, endVal, {
						decimalPlaces: 0, // 小数位数
						separator: ',', // 千位分隔符
					}).start();
				});
			});
		},
		initTime() {
			this.time.txt = formatDate(new Date(), 'YYYY-mm-dd HH:MM:SS WWW');
			this.time.fun = setInterval(() => {
				this.time.txt = formatDate(new Date(), 'YYYY-mm-dd HH:MM:SS WWW');
			}, 1000);
		},
		// 请求结果占比
		initRequestRatios() {
			if (!this.global.dispose.some((b) => b === this.global.homeChartOne)) this.global.homeChartOne.dispose();
			this.global.homeChartOne = echarts.init(this.$refs.requestRatios, this.charts.theme);
			const option = {
				backgroundColor: this.charts.bgColor,
				color: ['#F56C6C', '#67C23A', '#E6A23C'],
				grid: {
					top: 50,
					right: 20,
					bottom: 30,
					left: 30,
				},
				tooltip: {
					trigger: 'item',
					formatter: '{b} : {c} ({d}%)'
				},
				legend: {
					left: 'center',
				},
				series: [
					{
						type: 'pie',
						radius: ['40%', '70%'],
						avoidLabelOverlap: false,
						itemStyle: {
							borderRadius: 10,
							borderColor: '#fff',
							borderWidth: 2,
						},
						data: this.requestRatioData,
						top: 30,
					},
				],
			};
			this.global.homeChartOne.setOption(option);
			window.addEventListener('resize', () => {
				this.global.homeChartOne.resize();
			});
		},

		initTestCaseCreationTop5() {
			if (!this.global.dispose.some((b) => b === this.global.homeChartTwo)) this.global.homeChartTwo.dispose();
			this.global.homeChartTwo = echarts.init(this.$refs.testCaseCreationTop5, this.charts.theme);
			const option = {
				backgroundColor: this.charts.bgColor,
				dataset: [
					{
						dimensions: ['name', 'caseCount'],
						source: this.testCaseCreationTop5.map(item => [item.name, item.value])
					},
					{
						transform: {
							type: 'sort',
							config: { dimension: 'caseCount', order: 'desc' }
						}
					}
				],
				tooltip: { // 建议加上 tooltip，体验更好
					trigger: 'axis',
					axisPointer: {
						type: 'shadow'
					}
				},
				grid: { // 调整 grid 避免标签被截断
					left: '3%',
					right: '4%',
					bottom: '3%',
					containLabel: true
				},
				xAxis: {
					type: 'category',
					axisLabel: { interval: 0, rotate: 30 }
				},
				yAxis: {
					name: '创建用例数量'
				},
				series: {
					type: 'bar',
					encode: { x: 'name', y: 'caseCount' },
					datasetIndex: 1
				}
			};
			this.global.homeChartTwo.setOption(option);
			window.addEventListener('resize', () => {
				this.global.homeChartTwo.resize();
			});
		},
		// 缺货监控
		initRequestDailyDetail() {
			if (!this.global.dispose.some((b) => b === this.global.homeChartThree)) this.global.homeChartThree.dispose();
			this.global.homeChartThree = echarts.init(this.$refs.requestDailyDetail, this.charts.theme);
			const option = {
				backgroundColor: this.charts.bgColor,
				grid: {
					top: 50,
					right: 20,
					bottom: 30,
					left: 30,
				},
				tooltip: {
					trigger: 'axis',
				},
				legend: {
					data: ['单接口请求', '链路请求', '成功数量', '失败数量', '异常数量'],
					// data: ['订单数量', '超时数量', '在线数量', '预警数量'],
					right: 13,
				},
				xAxis: {
					data: this.requestDailyDetails.map(item => item.dateTime),
				},
				yAxis: [
					{
						type: 'value',
						name: '数量',
					},
				],
				series: [
					{
						name: '单接口请求',
						type: 'bar',
						data: this.requestDailyDetails.map(item => item.singleRequestCount),
					},
					{
						name: '链路请求',
						type: 'bar',
						data: this.requestDailyDetails.map(item => item.chainRequestCount),
					},
					{
						name: '成功数量',
						type: 'line',
						data: this.requestDailyDetails.map(item => item.successCount),
					},
					{
						name: '失败数量',
						type: 'line',
						data: this.requestDailyDetails.map(item => item.failCount),
					},
					{
						name: '异常数量',
						type: 'line',
						data: this.requestDailyDetails.map(item => item.errorCount),
					},
				],
			};
			this.global.homeChartThree.setOption(option);
			window.addEventListener('resize', () => {
				this.global.homeChartThree.resize();
			});
		},
		// 随机语录
		initDailyMessage() {
			this.dailyMessage = dailyMessage[Math.floor(Math.random() * dailyMessage.length)];
		},
		// 消息通知当前项点击
		onNewsInfoListClick(v) {
			window.open(v.link);
		},
		...mapActions('message', ['setRecentMessageList'])
	},
	watch: {
		// 监听 vuex 数据变化
		'$store.state.themeConfig.themeConfig.isIsDark': {
			handler(isIsDark) {
				this.$nextTick(() => {
					this.charts.theme = isIsDark ? 'dark' : '';
					this.charts.bgColor = isIsDark ? 'transparent' : '';
					setTimeout(() => {
						this.initRequestRatios();
					}, 500);
					setTimeout(() => {
						this.initTestCaseCreationTop5();
					}, 700);
					setTimeout(() => {
						this.initRequestDailyDetail();
					}, 1000);
				});
			},
			deep: true,
			immediate: true,
		},
	},
};
</script>

<style scoped lang="scss">
@import './index.scss';
</style>
