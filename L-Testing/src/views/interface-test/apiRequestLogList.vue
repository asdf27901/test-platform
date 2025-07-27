<template>
    <div class="log-list-container">
        <!-- 1. 搜索区域 -->
        <div class="search-area">
            <el-form :model="searchForm" label-width="80px" @submit.native.prevent>
                <el-row :gutter="20" class="search-row">
                    <el-col :span="6">
                        <el-form-item label="日志ID">
                            <el-input v-model.trim="searchForm.id" placeholder="请输入日志ID" clearable></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="接口ID">
                            <el-input v-model.trim="searchForm.interfaceId" placeholder="请输入接口ID" clearable></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="用例ID">
                            <el-input v-model.trim="searchForm.testcaseId" placeholder="请输入用例ID" clearable></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="用例名称">
                            <el-input v-model.trim="searchForm.testcaseName" placeholder="请输入用例名称" clearable></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20" class="search-row">
                    <el-col :span="6">
                        <el-form-item label="执行人">
                            <el-select v-model="searchForm.executorId" placeholder="请选择执行人" clearable filterable style="width: 100%;">
                                <el-option
                                    v-for="user in users"
                                    :key="user.id"
                                    :label="user.nickName"
                                    :value="user.id"
                                ></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="链路ID">
                            <el-input v-model.trim="searchForm.chainId" placeholder="请输入链路ID" clearable></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="执行结果">
                            <el-select v-model="searchForm.executeResult" placeholder="请选择执行结果" clearable style="width: 100%;">
                                <el-option label="成功" :value="1"></el-option>
                                <el-option label="失败" :value="0"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="请求类型">
                            <el-select v-model="searchForm.requestType" placeholder="请选择请求类型" clearable style="width: 100%;">
                                <el-option label="单接口请求" :value="0"></el-option>
                                <el-option label="链路请求" :value="1"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20">
                    <el-col :span="8">
                        <el-form-item label="执行时间">
                            <el-date-picker
                                v-model="searchForm.executionTimeRange"
                                type="datetimerange"
                                range-separator="至"
                                start-placeholder="开始日期"
                                end-placeholder="结束日期"
                                value-format="yyyy-MM-dd HH:mm:ss"
                                style="width: 100%;"
                            ></el-date-picker>
                        </el-form-item>
                    </el-col>
                    <el-col :span="16" style="text-align: right;">
                        <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜 索</el-button>
                        <el-button icon="el-icon-refresh" @click="handleReset">重 置</el-button>
                    </el-col>
                </el-row>
            </el-form>
        </div>

        <!-- 2. 表格区域 -->
        <el-table
            :data="tableData"
            v-loading="loading"
            border
            style="width: 100%"
        >
            <el-table-column prop="id" label="日志ID" min-width="100" align="center"></el-table-column>

            <el-table-column label="执行目标" min-width="320">
                <template slot-scope="scope">
                    <!-- 情况1: 链路执行 (最高优先级) -->
                    <div v-if="scope.row.chainId" class="target-cell">
                        <el-tag size="small">链路</el-tag>
                        <a @click="navigateTo(scope.row.chainId)" class="link-style target-id">
                            链路ID: {{ scope.row.chainId }}
                        </a>
                    </div>

                    <!-- 情况2: 用例执行 -->
                    <div v-else-if="scope.row.testcaseId" class="target-cell">
                        <el-tag type="success" size="small">用例</el-tag>
                        <div class="target-content">
                            <span class="target-name">{{ scope.row.testcaseName }}</span>
                            <div class="target-sub-id">
                                <!-- 将 "接口ID: " 和 ID数字 一起放在 <a> 标签内 -->
                                <a @click="
                                    navigateTo({
                                        name: 'InterfaceList',
                                        params: {interfaceId: scope.row.interfaceId}
                                    })"
                                   class="link-style">
                                    接口ID: {{ scope.row.interfaceId }}
                                </a>

                                <span class="separator">|</span>

                                <!-- 将 "用例ID: " 和 ID数字 一起放在 <a> 标签内 -->
                                <a @click="
                                    navigateTo({
                                        path: '/interfaces-test/testcaseList/editTestcase',
                                        query: {testcaseId: scope.row.testcaseId}
                                    })"
                                    class="link-style">
                                    用例ID: {{ scope.row.testcaseId }}
                                </a>
                            </div>
                        </div>
                    </div>

                    <!-- 情况3: 单接口请求 (有接口ID) -->
                    <div v-else-if="scope.row.interfaceId" class="target-cell">
                        <el-tag type="info" size="small">执行时未关联用例</el-tag>
                        <a @click="
                                    navigateTo({
                                        name: 'InterfaceList',
                                        params: {interfaceId: scope.row.interfaceId}
                                    })"
                           class="link-style target-id">
                            接口ID: {{ scope.row.interfaceId }}
                        </a>
                    </div>

                    <!-- 情况4: 无法识别来源 (所有ID都为空) -->
                    <div v-else class="target-cell">
                        <el-tag type="danger" size="small">异常</el-tag>
                        <span class="target-sub-id">（执行异常）</span>
                    </div>
                </template>
            </el-table-column>

            <el-table-column label="请求类型" min-width="120" align="center">
                <template slot-scope="scope">
                    <el-tag :type="scope.row.requestType === 0 ? 'primary' : 'success'" size="small">
                        {{ scope.row.requestType === 0 ? '单接口请求' : '链路请求' }}
                    </el-tag>
                </template>
            </el-table-column>

            <el-table-column label="执行结果" min-width="100" align="center">
                <template slot-scope="scope">
                    <el-tag
                        :type="scope.row.executeResult === 1 ? 'success' : 'danger'"
                        size="small"
                    >
                        {{ scope.row.executeResult === 1 ? '成功' : (scope.row.executeResult === 0 ? '失败' : '异常') }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="executorName" label="执行人" min-width="120" align="center"></el-table-column>
            <el-table-column prop="executionTime" label="执行时间" min-width="180" align="center"></el-table-column>

            <el-table-column label="操作" min-width="120" align="center">
                <template slot-scope="scope">
                    <el-button type="text" icon="el-icon-view" @click="$router.push({name: 'ApiRequestLogDetail', query: {id: scope.row.id}})">查看详情</el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 3. 分页区域 -->
        <div class="pagination-area">
            <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="pagination.current"
                :page-sizes="[10, 20, 50, 100]"
                :page-size="pagination.size"
                layout="total, sizes, prev, pager, next, jumper"
                :total="pagination.total">
            </el-pagination>
        </div>
    </div>
</template>

<script>
import { userApis } from "@/api/user";
import { Message } from "element-ui";
import { LogsApi } from "@/api/interface/logs";
import row from "element-ui/packages/row";

const getInitialSearchForm = () => ({
    id: '',
    interfaceId: '',
    testcaseId: '',
    testcaseName: '',
    executorId: '',
    chainId: '', // 新增
    requestType: null, // 新增
    executeResult: null,
    executionTimeRange: [],
});


export default {
    name: 'ApiRequestLogList',
    computed: {
        row() {
            return row
        }
    },
    data() {
        return {
            loading: false,
            users: [],
            searchForm: getInitialSearchForm(),
            tableData: [],
            pagination: {
                current: 1,
                size: 10,
                total: 0
            }
        };
    },
    created() {
        this.fetchData();
        this.fetchActiveUser();
    },
    methods: {
        async fetchActiveUser() {
            try {
                const { data } = await userApis.getActiveUserList();
                this.users = data;
            } catch (e) {
                if (e.code) {
                    Message.error(e.message);
                }
            }
        },
        async fetchData() {
            this.loading = true;

            const params = {
                current: this.pagination.current,
                size: this.pagination.size,
            };
            // 清理搜索表单中的空值
            for (const key in this.searchForm) {
                if (this.searchForm[key] !== '' && this.searchForm[key] !== null && this.searchForm[key].length !== 0) {
                    params[key] = this.searchForm[key];
                }
            }

            if (this.searchForm.executionTimeRange && this.searchForm.executionTimeRange.length === 2) {
                params.begin = this.searchForm.executionTimeRange[0];
                params.end = this.searchForm.executionTimeRange[1];
            }
            delete params.executionTimeRange

            try {
                const { data } = await LogsApi.getApiRequestLogsList(params); // mockApi需要更新以处理新字段
                this.tableData = data.records
                this.pagination.total = data.total
            } catch (e) {
                if (e.code) {
                    Message.error(e.message)
                }
            } finally {
                this.loading = false;
            }
        },
        navigateTo(pathObj) {
            this.$router.push(pathObj)
        },
        handleSearch() {
            this.pagination.currentPage = 1;
            this.fetchData();
        },
        handleReset() {
            this.searchForm = getInitialSearchForm();
            this.handleSearch();
        },
        handleSizeChange(val) {
            this.pagination.size = val;
            this.fetchData();
        },
        handleCurrentChange(val) {
            this.pagination.current = val;
            this.fetchData();
        }
    }
};
</script>

<style scoped>
.log-list-container {
    padding: 20px;
}

/* 搜索区域样式调整 */
.search-area {
    background-color: #ffffff; /* 1. 背景改为白色 */
    border: 1px solid #e4e7ed; /* 增加一个细边框 */
    padding: 20px 20px 0 20px; /* 底部padding设为0，由行间距控制 */
    border-radius: 6px;
    margin-bottom: 20px;
}

/* 2. 为搜索行增加下外边距 */
.search-area .search-row {
    margin-bottom: 20px;
}

/* 修正：确保最后一行也有底部间距，但不对form-item直接操作 */
.search-area .el-form {
    padding-bottom: 20px;
}
/* 移除之前可能导致冲突的样式 */
.search-area .el-row:last-child {
    margin-bottom: 0;
}


.pagination-area {
    margin-top: 20px;
    text-align: right;
}

.target-cell {
    display: flex;
    align-items: center;
}

.target-cell .el-tag {
    margin-right: 10px;
    flex-shrink: 0;
}

.target-id {
    color: #333;
    font-weight: 500;
}

.target-content {
    display: flex;
    flex-direction: column;
}

.target-name {
    font-weight: 500;
    color: #333;
}

.link-style {
    color: #409EFF; /* Element UI 的主题蓝色 */
    cursor: pointer;
    text-decoration: none; /* 默认无下划线 */
}
.link-style:hover {
    text-decoration: underline; /* 鼠标悬停时显示下划线 */
}
.target-id.link-style {
    font-weight: 500; /* 让单行ID的链接字体稍粗，但不是等宽字体 */
}
/* 分隔符样式 */
.separator {
    margin: 0 8px;
    color: #DCDFE6;
}

/* 调整一下 .target-sub-id 的布局 */
.target-sub-id {
    font-size: 12px;
    color: #999;
    margin-top: 2px;
    display: flex; /* 使用flex布局让元素在同一行 */
    align-items: center;
}
</style>
