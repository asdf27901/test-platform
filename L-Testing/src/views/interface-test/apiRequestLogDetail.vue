<template>
    <div class="log-detail-page" v-loading="loading">
        <!-- 1. 页面头部 -->
        <div class="page-title">接口请求记录详情 (ID: {{ logId }})</div>

        <div v-if="logDetail">
            <!-- 2. 总体执行信息 -->
            <el-card shadow="never" style="margin-bottom: 15px;">
                <div slot="header" class="clearfix">
                    <span>总体执行信息</span>
                </div>
                <el-descriptions :column="3" border>
                    <el-descriptions-item label="执行人">{{ logDetail.executorName }} (ID: {{ logDetail.executorId }})</el-descriptions-item>
                    <el-descriptions-item label="执行时间">{{ logDetail.executionTime }}</el-descriptions-item>
                    <el-descriptions-item label="请求类型">
                        <el-tag :type="logDetail.requestType === 0 ? 'primary' : 'success'" size="small">
                            {{ logDetail.requestType === 0 ? '单接口请求' : '链路请求' }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="最终结果" :span="1">
                        <el-tag :type="logDetail.executeResult === 1 ? 'success' : 'danger'" size="small">
                            {{ logDetail.executeResult === 1 ? '成功' : (logDetail.executeResult === 0 ? '失败' : '异常') }}
                        </el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item v-if="logDetail.executeResult === -1" label="总体错误" :span="1">
                        <span style="color: #F56C6C;">{{ logDetail.errorMsg || '无详细异常信息' }}</span>
                    </el-descriptions-item>
                </el-descriptions>
            </el-card>

            <!-- 3. 主体三栏布局 -->
            <el-row :gutter="15" v-if="logDetail.steps.length > 0">
                <!-- 左栏：步骤导航 -->
                <el-col :span="6">
                    <el-card shadow="never" class="column-card">
                        <div slot="header" class="clearfix">
                            <span>执行接口用例</span>
                        </div>
                        <ul class="step-list">
                            <li
                                v-for="(step, index) in logDetail.steps"
                                :key="index"
                                class="step-item"
                                :class="{ 'is-active': selectedIndex === index }"
                                @click="selectStep(index)"
                            >
                                <!-- 修改点：使用图标展示序号 -->
                                <span class="step-index-icon">{{ index + 1 }}</span>
                                <span class="step-name" :title="getStepDisplayName(step)">{{ getStepDisplayName(step) }}</span>

                                <i v-if="step.executeResult === 1" class="el-icon-success step-icon success"></i>
                                <i v-else-if="step.executeResult === 0" class="el-icon-error step-icon danger"></i>
                                <i v-else class="el-icon-warning step-icon danger"></i>
                            </li>
                        </ul>
                    </el-card>
                </el-col>

                <!-- 中栏：基本信息摘要 -->
                <el-col :span="8">
                    <el-card shadow="never" class="column-card">
                        <div slot="header" class="clearfix">
                            <span>接口 {{ selectedIndex + 1 }} - 基本信息</span>
                        </div>
                        <div v-if="selectedStep" class="card-body">
                            <el-descriptions :column="1" border>
                                <el-descriptions-item label="接口名称">
                                    <span v-if="selectedStep.interfaceId && selectedStep.interfaceName"
                                          class="clickable-link"
                                          @click="$router.push({
                                            name: 'InterfaceList',
                                            params: {interfaceId: selectedStep.interfaceId}
                                          })"
                                    >
                                        {{ selectedStep.interfaceName }} (ID: {{ selectedStep.interfaceId }})
                                    </span>
                                    <span v-else-if="selectedStep.interfaceId">
                                        接口已被删除
                                    </span>
                                    <span v-else>执行时未关联接口</span>
                                </el-descriptions-item>
                                <el-descriptions-item label="用例名称">
                                    <span v-if="selectedStep.testcaseId && selectedStep.testcaseName"
                                          class="clickable-link"
                                          @click="$router.push({
                                            path: '/interfaces-test/testcaseList/editTestcase',
                                            query: {testcaseId: selectedStep.testcaseId}
                                          })"
                                    >
                                        {{ selectedStep.testcaseName || '用例已被删除' }} (ID: {{ selectedStep.testcaseId }})
                                    </span>
                                    <span v-else-if="selectedStep.testcaseId">
                                        用例已被删除
                                    </span>
                                    <span v-else>执行时未关联用例</span>
                                </el-descriptions-item>
                                <el-descriptions-item label="步骤结果">
                                    <el-tag :type="selectedStep.executeResult === 1 ? 'success' : 'danger'" size="small">
                                        {{ selectedStep.executeResult === 1 ? '成功' : (selectedStep.executeResult === 0 ? '失败': '异常') }}
                                    </el-tag>
                                </el-descriptions-item>
                                <el-descriptions-item v-if="selectedStep.executeResult === -1" label="异常信息">
                                    <span style="color: #F56C6C;">{{ selectedStep.errorMsg || '无详细异常信息' }}</span>
                                </el-descriptions-item>
                            </el-descriptions>
                        </div>
                    </el-card>
                </el-col>

                <!-- 右栏：请求/响应详情 -->
                <el-col :span="10">
                    <el-card shadow="never" class="column-card">
                        <div slot="header" class="clearfix">
                            <span>接口 {{ selectedIndex + 1 }} - 请求数据</span>
                        </div>
                        <div class="card-body">
                            <ExecutionDetailCollapse v-if="selectedStepExecutionData" :stepData="selectedStepExecutionData" />
                        </div>
                    </el-card>
                </el-col>
            </el-row>

            <el-card shadow="never" v-else>
                <div slot="header" class="clearfix">
                    <span>执行接口用例</span>
                </div>
                <el-empty description="该请求记录没有执行接口用例"></el-empty>
            </el-card>
        </div>
    </div>
</template>

<script>
import { LogsApi } from "@/api/interface/logs";
import { Message } from "element-ui";
import ExecutionDetailCollapse from './components/ExecutionDetailCollapse.vue';

export default {
    name: "ApiRequestLogDetail",
    components: {
        ExecutionDetailCollapse
    },
    data() {
        return {
            loading: true,
            logId: null,
            logDetail: null,
            selectedIndex: 0,
        };
    },
    computed: {
        selectedStep() {
            return this.logDetail.steps[this.selectedIndex];
        },
        selectedStepExecutionData() {
            if (!this.selectedStep) return null;
            return {
                request: this.selectedStep.requestData,
                response: this.selectedStep.responseData,
                preScript: this.selectedStep.preScriptData,
                postScript: this.selectedStep.postScriptData,
            };
        }
    },
    created() {
        this.logId = this.$route.query.id ? + this.$route.query.id + "" : null
        if (!this.logId) {
            Message.error("未提供日志ID")
            this.goBack()
            return
        }
        this.fetchLogDetail();
    },
    activated() {
        const logId = this.$route.query.id ? + this.$route.query.id + "" : null
        if (!logId) {
            Message.error("未提供日志ID");
            this.goBack()
            return
        }
        if (logId !== this.logId) {
            this.logId = logId
            this.fetchLogDetail();
        }
    },
    methods: {
        async fetchLogDetail() {
            this.loading = true;
            this.logDetail = null
            try {
                const { data } = await LogsApi.getApiRequestLogById(this.logId);
                this.logDetail = data;
                this.selectedIndex = 0;
            } catch (e) {
                if (e.code) {
                    Message.error(e.message)
                }
                this.goBack()
            } finally {
                this.loading = false;
            }
        },
        selectStep(index) {
            this.selectedIndex = index;
        },
        getStepDisplayName(step) {
            if (step.testcaseName) return step.testcaseName
            if (step.testcaseId) return '用例已被删除'
            return '执行时未关联测试用例'
        },
        goBack() {
            const path = this.$route.path
            this.bus.$emit('closeTagsView', {
                path,
                redirect: '/interfaces-test/apiRequestLog'
            })
        }
    }
};
</script>

<style scoped>
.log-detail-page {
    padding: 20px;
    background-color: #ffffff;
    min-height: calc(100vh - 50px);
}
.page-title {
    font-size: 20px;
    font-weight: 500;
    color: #303133;
    margin-bottom: 20px;
}
.column-card {
    height: calc(100vh - 255px);
    display: flex;
    flex-direction: column;
}
.card-body {
    flex-grow: 1;
    overflow-y: auto;
    padding: 20px;
}
.el-card ::v-deep .el-card__header {
    padding: 15px 20px;
    flex-shrink: 0;
}
.el-card ::v-deep .el-card__body {
    padding: 0;
    overflow-y: auto;
}
.step-list {
    list-style: none;
    padding: 15px;
    margin: 0;
}
.step-item {
    padding: 10px 15px;
    cursor: pointer;
    border-radius: 4px;
    margin-bottom: 8px;
    transition: all 0.2s;
    display: flex;
    justify-content: space-between;
    align-items: center;
    border: 1px solid #e4e7ed;
}
.step-item:hover { background-color: #f5f7fa; }
.step-item.is-active {
    background-color: #ecf5ff;
    border-color: #b3d8ff;
    font-weight: 500;
}
.step-item.is-active .step-name {
    color: #409EFF;
}

.step-index-icon {
    flex-shrink: 0;
    width: 22px;
    height: 22px;
    border-radius: 50%;
    background-color: #e4e7ed;
    color: #606266;
    /* 替换 flex 布局为 line-height 垂直居中 */
    text-align: center;
    line-height: 20px; /* 关键：使其等于高度 */
    font-size: 13px;
    font-weight: 600;
    margin-right: 12px;
    transition: all 0.2s;
}

.step-item.is-active .step-index-icon {
    background-color: #409EFF;
    color: #ffffff;
}

.step-name {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    flex-grow: 1;
    margin-right: 10px;
    color: #303133;
    transition: color 0.2s;
}
.step-icon { font-size: 16px; flex-shrink: 0; }
.step-icon.success { color: #67C23A; }
.step-icon.danger { color: #F56C6C; }
.clickable-link {
    color: #409EFF;
    cursor: pointer;
    transition: color 0.2s;
}

.clickable-link:hover {
    color: #66b1ff;
    text-decoration: underline;
}
</style>
