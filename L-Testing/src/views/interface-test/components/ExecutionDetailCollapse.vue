<template>
    <el-collapse v-model="activeCollapse">
        <!-- 1. 请求数据 (Request Data) -->
        <el-collapse-item name="1">
            <template slot="title">
                <span class="collapse-title">
                    <b :class="{'is-active': activeCollapse.includes('1')}">
                        {{ activeCollapse.includes('1') ? '[ 点击收起 ]' : '[ 点击展开 ]' }}
                    </b>
                    请求数据 (Request Data)
                </span>
            </template>
            <div v-if="hasData(stepData.request)" class="detail-section">
                <!-- 基本信息 -->
                <el-descriptions :column="1" border size="small">
                    <el-descriptions-item label="请求方法">
                        <el-tag :type="getMethodTagType(stepData.request.method)" size="small">{{ stepData.request.method }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="请求地址 (Host)">{{ stepData.request.host }}</el-descriptions-item>
                    <el-descriptions-item label="请求路径 (Path)">{{ stepData.request.path }}</el-descriptions-item>
                </el-descriptions>

                <!-- 查询参数 (Query Parameters) -->
                <div v-if="hasData(stepData.request.queryParam)" class="sub-section">
                    <h3 class="sub-section-title">查询参数 (Query)</h3>
                    <el-table :data="queryParamsToArray(stepData.request.queryParam)" stripe border size="small">
                        <el-table-column prop="key" label="参数名" width="200"></el-table-column>
                        <el-table-column prop="value" label="参数值"></el-table-column>
                    </el-table>
                </div>

                <!-- 请求头 (Headers) -->
                <div v-if="hasData(stepData.request.headers)" class="sub-section">
                    <h3 class="sub-section-title">请求头 (Headers)</h3>
                    <el-table :data="objectToArray(stepData.request.headers)" stripe border size="small">
                        <el-table-column prop="key" label="key" width="200"></el-table-column>
                        <el-table-column prop="value" label="value"></el-table-column>
                    </el-table>
                </div>

                <!-- 新增：表单数据 (Form Data) -->
                <div v-if="hasData(stepData.request.form)" class="sub-section">
                    <h3 class="sub-section-title">表单数据 (Form Data)</h3>
                    <el-table :data="stepData.request.form" stripe border size="small">
                        <el-table-column prop="key" label="key" width="200"></el-table-column>
                        <el-table-column prop="value" label="value"></el-table-column>
                        <el-table-column prop="type" label="type" width="120">
                            <template slot-scope="scope">
                                <el-tag type="success" size="small">{{scope.row.type === 'file' ? '文件 (File)' : '文本(text)'}}</el-tag>
                            </template>
                        </el-table-column>
                    </el-table>
                </div>

                <!-- 请求体 (Body) -->
                <div v-if="hasData(stepData.request.body)" class="sub-section">
                    <h3 class="sub-section-title">请求体 (Body)</h3>
                    <div class="json-pretty-container">
                        <vue-json-pretty :data="getParsableData(stepData.request.body)" :deep="3" show-length show-icon virtual/>
                    </div>
                </div>

            </div>
            <el-tag type="info" v-else>无请求数据</el-tag>
        </el-collapse-item>

        <!-- 2. 响应数据 (Response Data) -->
        <el-collapse-item name="2">
            <template slot="title">
                 <span class="collapse-title">
                    <b :class="{'is-active': activeCollapse.includes('2')}">
                        {{ activeCollapse.includes('2') ? '[ 点击收起 ]' : '[ 点击展开 ]' }}
                    </b>
                    响应数据 (Response Data)
                </span>
            </template>
            <div v-if="hasData(stepData.response)" class="detail-section">
                <!-- 基本信息 -->
                <el-descriptions :column="2" border size="small">
                    <el-descriptions-item label="状态码 (Status)">
                        <el-tag :type="getStatusTagType(stepData.response.statusCode)" size="small">{{ stepData.response.statusCode }}</el-tag>
                    </el-descriptions-item>
                    <el-descriptions-item label="响应耗时 (Time)">{{ stepData.response.responseTime }} ms</el-descriptions-item>
                </el-descriptions>

                <!-- 响应头 (Headers) -->
                <div v-if="hasData(stepData.response.headers)" class="sub-section">
                    <h3 class="sub-section-title">响应头 (Headers)</h3>
                    <el-table :data="objectToArray(stepData.response.headers)" stripe border size="small">
                        <el-table-column prop="key" label="key" width="200"></el-table-column>
                        <el-table-column prop="value" label="value"></el-table-column>
                    </el-table>
                </div>

                <!-- Cookies -->
                <div v-if="hasData(stepData.response.cookies)" class="sub-section">
                    <h3 class="sub-section-title">Cookies</h3>
                    <el-table :data="stepData.response.cookies" stripe border size="small">
                        <el-table-column prop="name" label="Name" width="180"></el-table-column>
                        <el-table-column prop="value" label="Value"></el-table-column>
                        <el-table-column prop="domain" label="Domain" width="150"></el-table-column>
                        <el-table-column prop="path" label="Path" width="100"></el-table-column>
                        <el-table-column prop="maxAge" label="Max-Age" width="100"></el-table-column>
                    </el-table>
                </div>

                <!-- 响应体 (Body) -->
                <div v-if="hasData(stepData.response.body)" class="sub-section">
                    <h3 class="sub-section-title">响应体 (Body)</h3>
                    <div class="json-pretty-container">
                        <vue-json-pretty :data="stepData.response.body" :deep="3" show-length show-icon virtual/>
                    </div>
                </div>
            </div>
            <el-tag type="info" v-else>无响应数据</el-tag>
        </el-collapse-item>

        <el-collapse-item name="3">
            <template slot="title">
                 <span class="collapse-title">
                    <b :class="{'is-active': activeCollapse.includes('3')}">
                        {{ activeCollapse.includes('3') ? '[ 点击收起 ]' : '[ 点击展开 ]' }}
                    </b>
                    前置脚本记录 (Pre-script Log)
                </span>
            </template>
            <div class="detail-section" v-if="hasData(stepData.request.preScript) || hasData(stepData.preScript)">
                <!-- 脚本代码 -->
                <div v-if="hasData(stepData.request.preScript)" class="sub-section">
                    <h3 class="sub-section-title">脚本代码 (Script Code)</h3>
                    <pre class="script-code-viewer">{{ stepData.request.preScript }}</pre>
                </div>
                <!-- 执行日志 -->
                <div v-if="hasData(stepData.preScript) && (hasData(stepData.preScript.logs) || hasData(stepData.preScript.error))" class="sub-section">
                    <h3 class="sub-section-title">执行终端日志 (Execution Terminal)</h3>
                    <!-- 新增：终端风格的日志查看器 -->
                    <div class="terminal-viewer">
                        <!-- 显示 Logs -->
                        <div v-if="stepData.preScript.logs" class="terminal-line log">{{ stepData.preScript.logs }}</div>
                        <!-- 显示 Error -->
                        <div v-if="stepData.preScript.error" class="terminal-line error">
                            <i class="el-icon-error"></i> {{ stepData.preScript.error }}
                        </div>
                    </div>
                </div>
                <!-- 测试断言结果 -->
                <div v-if="hasData(stepData.preScript) && stepData.preScript.results && stepData.preScript.results.length > 0" class="sub-section">
                    <h3 class="sub-section-title">测试断言 (Test Assertions)</h3>
                    <ul class="assertion-list">
                        <li v-for="(assertion, index) in stepData.preScript.results" :key="index" class="assertion-item">
                            <el-tag :type="assertion.result ? 'success' : 'danger'" size="small" class="assertion-tag">
                                {{ assertion.result ? 'PASS' : 'FAIL' }}
                            </el-tag>
                            <div class="assertion-content">
                                <span class="assertion-name">{{ assertion.testName }}</span>
                                <div v-if="!assertion.result && assertion.message" class="assertion-message">
                                    {{ assertion.message }}
                                </div>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
            <el-tag type="info" v-else>无前置脚本信息</el-tag>
        </el-collapse-item>

        <el-collapse-item name="4">
            <template slot="title">
                 <span class="collapse-title">
                    <b :class="{'is-active': activeCollapse.includes('4')}">
                        {{ activeCollapse.includes('4') ? '[ 点击收起 ]' : '[ 点击展开 ]' }}
                    </b>
                    后置脚本记录 (Post-script Log)
                </span>
            </template>
            <div class="detail-section" v-if="hasData(stepData.request.postScript) || hasData(stepData.postScript)">
                <!-- 脚本代码 -->
                <div v-if="hasData(stepData.request.postScript)" class="sub-section">
                    <h3 class="sub-section-title">脚本代码 (Script Code)</h3>
                    <pre class="script-code-viewer">{{ stepData.request.postScript }}</pre>
                </div>
                <!-- 执行日志 -->
                <div v-if="hasData(stepData.postScript) && (hasData(stepData.postScript.logs) || hasData(stepData.postScript.error))" class="sub-section">
                    <h3 class="sub-section-title">执行终端日志 (Execution Terminal)</h3>
                    <!-- 新增：终端风格的日志查看器 -->
                    <div class="terminal-viewer">
                        <!-- 显示 Logs -->
                        <div v-if="stepData.postScript.logs" class="terminal-line log">{{ stepData.postScript.logs }}</div>
                        <!-- 显示 Error -->
                        <div v-if="stepData.postScript.error" class="terminal-line error">
                            <i class="el-icon-error"></i> {{ stepData.postScript.error }}
                        </div>
                    </div>
                </div>
                <!-- 测试断言结果 -->
                <div v-if="hasData(stepData.postScript) && stepData.postScript.results && stepData.postScript.results.length > 0" class="sub-section">
                    <h3 class="sub-section-title">测试断言 (Test Assertions)</h3>
                    <ul class="assertion-list">
                        <li v-for="(assertion, index) in stepData.postScript.results" :key="index" class="assertion-item">
                            <el-tag :type="assertion.result ? 'success' : 'danger'" size="small" class="assertion-tag">
                                {{ assertion.result ? 'PASS' : 'FAIL' }}
                            </el-tag>
                            <div class="assertion-content">
                                <span class="assertion-name">{{ assertion.testName }}</span>
                                <div v-if="!assertion.result && assertion.message" class="assertion-message">
                                    {{ assertion.message }}
                                </div>
                            </div>
                        </li>
                    </ul>
                </div>
            </div>
            <el-tag type="info" v-else>无后置脚本信息</el-tag>
        </el-collapse-item>

    </el-collapse>
</template>

<script>
import 'vue-json-pretty/lib/styles.css'
import VueJsonPretty from 'vue-json-pretty'
export default {
    name: 'ExecutionDetailCollapse',
    components: {
      VueJsonPretty
    },
    props: {
        stepData: {
            type: Object,
            required: true
        }
    },
    data() {
        return {
            activeCollapse: []
        };
    },
    methods: {
        hasData(data) {
            if (data === null || data === undefined) return false;
            if (typeof data === 'string' && data.trim() === '') return false;
            if (Array.isArray(data) && data.length === 0) return false;
            return !(typeof data === 'object' && !Array.isArray(data) && Object.keys(data).length === 0);
        },
        getParsableData(data) {
            if (typeof data === 'string') {
                try { return JSON.parse(data); } catch (e) { return { message: data }; }
            }
            return data;
        },
        // 新增：将对象转换为 [{key, value}] 格式的数组，以供 el-table 使用
        objectToArray(obj) {
            if (!obj) return [];
            return Object.entries(obj).map(([key, value]) => ({
                key,
                value: Array.isArray(value) ? value.join(', ') : value // 将数组值合并为字符串
            }));
        },
        queryParamsToArray(query) {
            if (!query || typeof query !== 'string') return [];
            const params = new URLSearchParams(query);
            return Array.from(params.entries()).map(([key, value]) => ({ key, value }));
        },
        // 新增：根据HTTP方法返回不同的el-tag类型
        getMethodTagType(method) {
            const m = method.toUpperCase();
            if (m === 'GET') return 'success';
            if (m === 'POST') return 'primary';
            if (m === 'PUT') return 'warning';
            if (m === 'DELETE') return 'danger';
            return 'info';
        },
        // 新增：根据HTTP状态码返回不同的el-tag类型
        getStatusTagType(status) {
            if (status >= 200 && status < 300) return 'success';
            if (status >= 400 && status < 500) return 'warning';
            if (status >= 500) return 'danger';
            return 'info';
        }
    }
}
</script>

<style scoped>
.detail-section {
    padding: 0 10px;
}
.collapse-title b {
    color: #409EFF;
}

.collapse-title b.is-active {
    color: #c76e25;
    margin-right: 8px;

}

.sub-section {
    margin-top: 20px;
}

.sub-section-title {
    font-size: 14px;
    font-weight: 600;
    color: #303133;
    margin-bottom: 10px;
    padding-left: 5px;
    border-left: 3px solid #409EFF;
}

.json-pretty-container {
    background-color: #fdfdfd;
    border: 1px solid #eef0f3;
    padding: 10px 15px;
    border-radius: 4px;
    font-size: 14px;
    font-family: 'Consolas', 'Courier New', Courier, monospace;
}

/*.script-code-viewer {*/
/*    font-weight: bolder;*/
/*}*/

/* --- 新增：终端查看器样式 --- */
.terminal-viewer {
    background-color: #1e1e1e; /* 深色背景 */
    color: #d4d4d4; /* 默认文字颜色 (白色) */
    padding: 15px;
    border-radius: 4px;
    font-family: 'Consolas', 'Courier New', 'Menlo', monospace;
    font-size: 14px;
    line-height: 1.6;
    white-space: pre-wrap; /* 保持换行 */
    word-break: break-all; /* 防止长字符串撑破容器 */
}

.terminal-line {
    margin-bottom: 5px; /* 行间距 */
}
.terminal-line:last-child {
    margin-bottom: 0;
}

.terminal-line.error {
    color: #f48771; /* 醒目的红色 */
}

.terminal-line.error .el-icon-error {
    margin-right: 5px; /* 图标和文字的间距 */
}
.assertion-list {
    list-style: none;
    padding: 0;
    margin: 0;
    border: 1px solid #e4e7ed;
    border-radius: 4px;
}
.assertion-item {
    display: flex;
    align-items: flex-start; /* 顶部对齐，以防 message 过长 */
    padding: 12px 15px;
    border-bottom: 1px solid #e4e7ed;
    font-size: 14px;
}
.assertion-item:last-child {
    border-bottom: none;
}
.assertion-tag {
    flex-shrink: 0;
    margin-right: 15px;
    font-weight: bold;
    width: 50px; /* 固定宽度，让 PASS/FAIL 对齐 */
    text-align: center;
}
.assertion-content {
    flex-grow: 1;
}
.assertion-name {
    color: #303133;
    display: block; /* 独占一行 */
}
.assertion-message {
    color: #F56C6C; /* 红色错误信息 */
    font-size: 12px;
    margin-top: 5px;
    white-space: pre-wrap; /* 保持换行 */
    word-break: break-all;
}
</style>
