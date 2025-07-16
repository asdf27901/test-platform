<template>
    <div class="api-test-container">
        <el-row :gutter="8" v-if="!pageLoading && !isSendingRequest">
            <!-- 左侧用例列表 -->
            <el-col :span="6" v-if="mode === 'add'">
                <el-card class="box-card test-cases-card" shadow="never">
                    <div
                            slot="header"
                            class="clearfix"
                            style="display: flex; justify-content: space-between; align-items: center; white-space: nowrap;"
                    >
                      <span style="font-weight: 600; font-size: 16px; margin-right: 8px;">
                        测试用例
                      </span>
                        <div style="display: flex; flex-direction: column; justify-content: center;">
                            <el-button
                                    size="mini"
                                    @click="generateDialogVisible = true"
                                    style="
                                        width: 70px;
                                        height: 26px;
                                        margin-bottom: 4px;
                                        background-color: #409EFF;
                                        color: white;
                                        border-color: #409EFF;
                                        padding: 0 8px;
                                        font-size: 12px;
                                        display: flex;
                                        align-items: center;
                                        justify-content: center;
                                      "
                            >
                                自动生成
                            </el-button>
                            <el-button
                                    size="mini"
                                    style="
                                    width: 70px;
                                    height: 26px;
                                    padding: 0 8px;
                                    font-size: 12px;
                                    display: flex;
                                    align-items: center;
                                    justify-content: center;
                                    color: #409EFF;
                                    border-color: #409EFF;
                                    background-color: white;
                                    margin-left: 0;
                                  "
                                    icon="el-icon-plus"
                                    @click="addTestCase"
                            >
                                <span style="margin-left: 4px;">新增</span>
                            </el-button>
                        </div>
                    </div>

                    <div class="test-case-list">
                        <div
                                v-for="item in testCases"
                                :key="item.id"
                                class="test-case-item"
                                :class="{ active: item.id === currentTestCase.id }"
                                @click="selectTestCase(item)"
                        >
                            <div class="case-content">
                                <!-- 错误提示图标 -->
                                <i v-if="item.errors && item.errors.length > 0" class="el-icon-warning error-icon"></i>
                                <span class="case-name">{{ item.name }}</span>
                            </div>
                            <span class="case-actions">
                                <el-button type="text" size="mini" @click.stop="deleteTestCase(item.id)">删除</el-button>
                            </span>
                        </div>
                    </div>
                </el-card>
            </el-col>

            <!-- 右侧主操作区 -->
            <el-col :span="mode === 'add' ? 18 : 24">
                <el-card class="box-card" shadow="never"  v-loading="isUploading" element-loading-text="文件上传中...">
                    <!-- 顶部操作区 -->
                    <div class="top-bar">
                        <div class="case-name-edit-wrapper" style="display: flex; align-items: center;">
                            <template v-if="editingTestCaseName">
                                <el-input
                                    v-model="editingTestCaseNameValue"
                                    size="small"
                                    style="width: 300px;"
                                    @blur="finishEditName"
                                    @keyup.enter.native="finishEditName"
                                    ref="nameInput"
                                    clearable
                                ></el-input>
                            </template>
                            <template v-else>
                                  <span style="font-size: 18px; font-weight: 600; user-select: none;">
                                    {{ currentTestCase.name || '未命名用例' }}
                                  </span>
                                <el-button
                                    type="text"
                                    icon="el-icon-edit"
                                    size="small"
                                    @click="startEditName"
                                    style="margin-left: 8px;"
                                ></el-button>

                                <span
                                    v-if="currentTestCase.errors && currentTestCase.errors.length > 0"
                                    class="error-text"
                                >
                                    {{ currentTestCase.errors[0] }}
                                </span>
                            </template>
                        </div>
                        <div style="display: flex; align-items: center;">
                            <span style="margin-right: 10px">优先级:</span>
                            <el-select
                                v-model="currentTestCase.priority"
                                placeholder="优先级"
                                style="width: 120px; margin-right: 10px;"
                            >
                                <el-option label="高" :value=0></el-option>
                                <el-option label="中" :value=1></el-option>
                                <el-option label="低" :value=2></el-option>
                            </el-select>

                            <span style="margin-right: 10px">测试环境: </span>
                            <el-select
                                v-model="currentEnvironment"
                                placeholder="请选择环境"
                                class="custom-env-select"
                                clearable
                            >
                                <el-option
                                    v-for="item in environments"
                                    :key="item.id"
                                    :label="item.name"
                                    :value="item.id"
                                ></el-option>
                            </el-select>
                        </div>
                    </div>

                    <!-- 请求构建区 -->
                    <div class="url-bar">
                        <div class="url-input-group">
                            <el-select v-model="currentTestCase.method" class="method-select">
                                <el-option label="POST" value="POST"/>
                                <el-option label="GET" value="GET"/>
                                <el-option label="PUT" value="PUT"/>
                                <el-option label="DELETE" value="DELETE"/>
                            </el-select>
                            <el-input v-model="host" placeholder="请输入Host" class="host-input"/>
                            <el-input v-model="currentTestCase.path" class="path-input" disabled/>
                        </div>

                        <!-- 路径参数输入框 -->
                        <el-input
                            v-model="currentTestCase.pathParam"
                            placeholder="请输入路径参数, 例如: /{1}/{xxxx}"
                            class="path-param-input"
                            clearable
                            :disabled="queryParamNum > 0"
                        />

                        <el-button
                            type="primary"
                            @click="sendRequest"
                            class="send-button"
                            icon="el-icon-position"
                        >
                            发送
                        </el-button>
                    </div>

                    <!-- 请求参数 Tabs -->
                    <el-tabs v-model="currentTestCase.requestTabsActive" class="request-tabs">
                        <el-tab-pane label="请求参数" name="params">
                            <el-table
                                :data="currentTestCase.queryParams"
                                style="width: 100%; font-size: 14px"
                                ref="paramsTable"
                                @select="handleParamsSelect"
                                @select-all="handleParamsSelectAll"
                            >
                                <el-table-column width="55" type="selection" align="center"/>
                                <el-table-column prop="key" label="Key">
                                    <template slot-scope="scope">
                                        <el-input v-model="scope.row.key" placeholder="Key"></el-input>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="value" label="Value">
                                    <template slot-scope="scope">
                                        <el-input v-model="scope.row.value" placeholder="Value"></el-input>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="description" label="备注">
                                    <template slot-scope="scope">
                                        <el-input v-model="scope.row.description" placeholder="备注"></el-input>
                                    </template>
                                </el-table-column>
                                <el-table-column label="操作" width="80">
                                    <template slot-scope="scope">
                                        <el-button
                                                type="danger"
                                                icon="el-icon-delete"
                                                circle
                                                size="mini"
                                                @click="removeQueryParam(scope.$index)"
                                        ></el-button>
                                    </template>
                                </el-table-column>
                            </el-table>
                            <el-button
                                    icon="el-icon-plus"
                                    @click="addQueryParam"
                                    style="margin-top: 10px;"
                                    :disabled="currentTestCase.pathParam.trim().length > 0"
                            >
                                添加参数
                            </el-button>
                        </el-tab-pane>

                        <el-tab-pane label="请求头" name="headers">
                            <el-table
                                :data="currentTestCase.headers"
                                style="width: 100%; font-size: 14px"
                                ref="headersTable"
                                @select="handleHeaderSelect"
                                @select-all="handleHeaderSelectAll"
                            >
                                <el-table-column width="55" align="center" type="selection">
                                </el-table-column>
                                <el-table-column prop="key" label="Key">
                                    <template slot-scope="scope">
                                        <el-input v-model="scope.row.key" placeholder="Key"></el-input>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="value" label="Value">
                                    <template slot-scope="scope">
                                        <el-input v-model="scope.row.value" placeholder="Value"></el-input>
                                    </template>
                                </el-table-column>
                                <el-table-column prop="description" label="备注">
                                    <template slot-scope="scope">
                                        <el-input v-model="scope.row.description" placeholder="备注"></el-input>
                                    </template>
                                </el-table-column>
                                <el-table-column label="操作" width="80">
                                    <template slot-scope="scope">
                                        <el-button
                                            type="danger"
                                            icon="el-icon-delete"
                                            circle
                                            size="mini"
                                            @click="removeHeader(scope.$index)"
                                        ></el-button>
                                    </template>
                                </el-table-column>
                            </el-table>
                            <el-button
                                icon="el-icon-plus"
                                @click="addHeader"
                                style="margin-top: 10px;"
                            >
                                添加请求头
                            </el-button>
                        </el-tab-pane>

                        <el-tab-pane label="请求体" name="body">
                            <div style="margin-bottom: 15px;">
                                <el-radio-group
                                    v-model="currentTestCase.requestBodyType"
                                    @input="handleRadioChange"
                                >
                                    <el-radio label="none">无</el-radio>
                                    <el-radio label="form-data">form-data</el-radio>
                                    <el-radio label="x-www-form-urlencoded">x-www-form-urlencoded</el-radio>
                                    <el-radio label="json">JSON</el-radio>
                                </el-radio-group>
                            </div>

                            <!-- 根据选择展示不同请求体编辑区域 -->
                            <div v-if="currentTestCase.requestBodyType === 'none'" style="color: #999; font-style: italic; text-align: center">
                                无请求体内容
                            </div>

                            <div v-if="currentTestCase.requestBodyType === 'form-data'">
                                <el-table
                                    :data="currentTestCase.formDataParams"
                                    style="width: 100%; font-size: 14px"
                                    ref="formDataTable"
                                    @select="handleFormDataSelect"
                                    @select-all="handleFormDataSelectAll"
                                >
                                    <el-table-column width="55" type="selection" align="center"></el-table-column>
                                    <el-table-column prop="key" label="Key">
                                        <template slot-scope="scope">
                                            <el-input v-model="scope.row.key" placeholder="Key"></el-input>
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="类型" prop="type" width="120">
                                        <template slot-scope="scope">
                                            <el-select v-model="scope.row.type" size="small" @change="onFormDataTypeChange(scope.row)">
                                                <el-option label="文本" value="text"></el-option>
                                                <el-option label="文件" value="file"></el-option>
                                            </el-select>
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="Value" prop="value">
                                        <template slot-scope="scope">
                                            <el-input v-if="scope.row.type === 'text'" v-model="scope.row.value" placeholder="Value"></el-input>
                                            <el-upload
                                                v-else
                                                :show-file-list="false"
                                                :http-request="(options) => handleFileUpload(options, scope.row, scope.$index)"
                                                :on-remove="() => onRemoveFile(scope.row)"
                                                :before-upload="file => onBeforeUpload(file)"
                                                :multiple="false"
                                                :limit="1"
                                                action=""
                                                :ref="`uploadRef_${scope.$index}`"
                                            >
                                                <div style="display: flex; align-items: center; width: 100%;">
                                                    <el-input
                                                        :value="scope.row.value"
                                                        placeholder="请选择文件"
                                                        readonly
                                                        style="flex-grow: 1;"
                                                    >
                                                        <el-button slot="append" icon="el-icon-close" @click.stop.prevent="onRemoveFile(scope.row)" v-if="scope.row.value"></el-button>
                                                    </el-input>
                                                    <el-button size="small" type="primary" style="margin-left: 8px;">选择文件</el-button>
                                                </div>
                                            </el-upload>
                                        </template>
                                    </el-table-column>
                                    <el-table-column prop="description" label="备注">
                                        <template slot-scope="scope">
                                            <el-input v-model="scope.row.description" placeholder="备注"></el-input>
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="操作" width="80">
                                        <template slot-scope="scope">
                                            <el-button
                                                type="danger"
                                                icon="el-icon-delete"
                                                circle
                                                size="mini"
                                                @click="removeFormDataParam(scope.$index)"
                                            ></el-button>
                                        </template>
                                    </el-table-column>
                                </el-table>
                                <el-button icon="el-icon-plus" @click="addFormDataParam" style="margin-top: 10px;">
                                    添加参数
                                </el-button>
                            </div>

                            <div v-if="currentTestCase.requestBodyType === 'x-www-form-urlencoded'">
                                <el-table
                                    :data="currentTestCase.urlEncodedParams"
                                    style="width: 100%; font-size: 14px"
                                    ref="urlEncodedTable"
                                    @select="handleUrlEncodedSelect"
                                    @select-all="handleUrlEncodedSelectAll"
                                >
                                    <el-table-column width="55" type="selection" align="center"></el-table-column>
                                    <el-table-column prop="key" label="Key">
                                        <template slot-scope="scope">
                                            <el-input v-model="scope.row.key" placeholder="Key"></el-input>
                                        </template>
                                    </el-table-column>
                                    <el-table-column prop="value" label="Value">
                                        <template slot-scope="scope">
                                            <el-input v-model="scope.row.value" placeholder="Value"></el-input>
                                        </template>
                                    </el-table-column>
                                    <el-table-column prop="description" label="备注">
                                        <template slot-scope="scope">
                                            <el-input v-model="scope.row.description" placeholder="备注"></el-input>
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="操作" width="80">
                                        <template slot-scope="scope">
                                            <el-button
                                                type="danger"
                                                icon="el-icon-delete"
                                                circle
                                                size="mini"
                                                @click="removeUrlEncodedParam(scope.$index)"
                                            ></el-button>
                                        </template>
                                    </el-table-column>
                                </el-table>
                                <el-button icon="el-icon-plus" @click="addUrlEncodedParam" style="margin-top: 10px;">
                                    添加参数
                                </el-button>
                            </div>

                            <div v-if="currentTestCase.requestBodyType === 'json'" class="code-json-editor" :key="currentTestCase.id">
                                <json-editor
                                    v-model="currentTestCase.jsonBody"
                                    :show-btns="false"
                                    mode="code"
                                    lang="zh"
                                    :expanded-on-start="true"
                                    ref="jsonEditor"
                                    @json-change="onJsonChange"
                                    @json-save="onJsonSave"
                                    @has-error="onError"
                                />
                            </div>
                        </el-tab-pane>

                        <el-tab-pane label="前置脚本 (Pre-Script)" name="preRequestScript">
                            <span slot="label">
                                前置脚本 (Pre-Script)
                                <i v-if="hasPreScript" class="script-indicator-dot"></i>
                            </span>
                            <ScriptEditor
                                v-if="currentTestCase.requestTabsActive === 'preRequestScript'"
                                v-model="currentTestCase.preRequestScript"
                                scriptType="preRequestScript"
                                ref="preRequestScript"
                            />
                        </el-tab-pane>

                        <el-tab-pane label="后置脚本 (Tests)" name="postRequestScript">
                            <span slot="label">
                                <span class="tab-label-content">
                                    后置脚本 (Tests)
                                    <i v-if="hasAssertionScript" class="script-indicator-dot"></i>
                                </span>
                            </span>
                            <ScriptEditor
                                v-if="currentTestCase.requestTabsActive === 'postRequestScript'"
                                v-model="currentTestCase.postRequestScript"
                                scriptType="postRequestScript"
                                ref="postRequestScript"
                            />
                        </el-tab-pane>
                    </el-tabs>

                    <!-- 响应区 -->
                    <div class="response-section">
                        <div class="section-title-bar">
                            <h3 class="section-title">响应</h3>
                            <div class="response-meta-info">
                                <!-- 状态码显示 -->
                                <div v-if="currentTestCase.response.statusCode" class="meta-item">
                                    <span class="meta-label">code:</span>
                                    <span class="meta-value" :style="{ color: statusCodeColor }">{{ currentTestCase.response.statusCode }}</span>
                                </div>
                                <!-- 耗时显示 -->
                                <div v-if="currentTestCase.response.responseTime" class="meta-item">
                                    <span class="meta-label">timeout:</span>
                                    <span class="meta-value" :style="{ color: responseTimeColor }">{{ currentTestCase.response.responseTime }} ms</span>
                                </div>
                            </div>
                        </div>

                        <el-tabs v-model="currentTestCase.responseTabsActive" type="border-card" class="response-tabs">
                            <el-tab-pane label="响应体" name="body">
                                <template v-if="currentTestCase.response.body !== null && currentTestCase.response.body !== undefined">
                                    <vue-json-pretty
                                        :data="currentTestCase.response.body"
                                        :deep="2"
                                        showLineNumber
                                        showIcon
                                        showDoubleQuotes
                                        showLength
                                        virtual
                                    />
                                </template>
                                <div v-else class="empty-response-placeholder">
                                    <div class="empty-icon-box">
                                        <i class="el-icon-receiving"></i>
                                    </div>
                                    <p class="empty-text-title">暂无响应数据</p>
                                    <span class="empty-text-subtitle">点击右上角的“发送”按钮以发起请求。</span>
                                </div>
                            </el-tab-pane>
                            <el-tab-pane label="Cookies" name="cookies">
                                <el-table :data="currentTestCase.response.cookies" style="width: 100%">
                                    <el-table-column prop="name" label="Name" align="center"/>
                                    <el-table-column prop="value" label="Value" align="center"/>
                                    <el-table-column prop="domain" label="Domain" align="center"/>
                                    <el-table-column prop="path" label="Path" align="center"/>
                                    <el-table-column prop="maxAge" label="MaxAge" align="center"/>
                                    <el-table-column label="HttpOnly" align="center">
                                        <template #default="{ row }">
                                            <el-tag type="success" v-if="row.httpOnly">是</el-tag>
                                            <el-tag v-else>否</el-tag>
                                        </template>
                                    </el-table-column>
                                    <el-table-column label="Secure" align="center">
                                        <template #default="{ row }">
                                            <el-tag type="success" v-if="row.secure">是</el-tag>
                                            <el-tag v-else>否</el-tag>
                                        </template>
                                    </el-table-column>
                                </el-table>
                            </el-tab-pane>
                            <el-tab-pane label="响应头" name="headers">
                                <el-table :data="responseHeadersArray" style="width: 100%">
                                    <el-table-column prop="key" label="Header" width="180" />
                                    <el-table-column label="Value">
                                        <template slot-scope="scope">
                                            <span v-if="Array.isArray(scope.row.value)">
                                                {{ scope.row.value.join(', ') }}
                                            </span>
                                            <span v-else>
                                                {{ scope.row.value }}
                                            </span>
                                        </template>
                                    </el-table-column>
                                </el-table>
                            </el-tab-pane>
                            <el-tab-pane label="控制台" name="console">
                                <!-- 使用 v-if 判断是否有日志内容 -->
                                <div v-if="consoleLogs" class="console-output-wrapper">
                                    <pre>{{ consoleLogs }}</pre>
                                </div>
                                <div v-else class="empty-placeholder">
                                    <p>没有控制台输出</p>
                                </div>
                            </el-tab-pane>
                            <el-tab-pane name="testResults">
                                <!-- 使用 slot="label" 来自定义标签，并显示通过/失败的数量 -->
                                <span slot="label">
                                    测试结果
                                     <span v-if="testResultsSummary.total > 0"
                                           :class="{ 'all-passed': testResultsSummary.allPassed, 'has-failures': !testResultsSummary.allPassed }">
                                        ({{ testResultsSummary.passed }}/{{ testResultsSummary.total }})
                                    </span>
                                </span>

                                <div v-if="testResultList && testResultList.length > 0">
                                    <div class="test-results-container">
                                        <div v-for="(result, index) in testResultList" :key="index" class="test-result-item">
                                            <div class="result-status-wrapper">
                                                <span :class="['result-status', result.result ? 'status-passed' : 'status-failed']">
                                                    {{ result.result ? 'PASS' : 'FAIL' }}
                                                </span>
                                            </div>

                                            <!-- 右侧的内容区 -->
                                            <div class="test-result-content">
                                                <!-- 头部：现在包含名称和来源标签 -->
                                                <div class="test-result-header">
                                                    <span class="test-name">{{ result.testName }}</span>
                                                    <el-tag size="mini" type="info" class="script-source-tag">{{ result.source }}</el-tag>
                                                </div>
                                                <div v-if="!result.result" class="test-message-wrapper">
                                                    <pre class="test-message">{{ result.message }}</pre>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div v-else class="empty-placeholder">
                                    <p>没有执行任何测试</p>
                                </div>
                            </el-tab-pane>
                        </el-tabs>
                    </div>
                    <!-- 底部按钮 -->
                    <div class="bottom-actions">
                        <el-button @click.stop="goBack">返回</el-button>
                        <el-button type="primary" @click.stop="saveOrUpdateTestcases">保存</el-button>
                    </div>
                </el-card>

            </el-col>
        </el-row>

        <el-card v-else class="box-card loading-card" shadow="never"
                 v-loading="true"
                 :element-loading-text="mainCardLoadingText"
                 element-loading-spinner="el-icon-loading">
        </el-card>

        <auto-generate-cases-dialog
            v-if="generateDialogVisible"
            :visible.sync="generateDialogVisible"
            :case-tpl-creator="createNewTestCaseData"
            @generate="handleGeneratedCases"
        />
    </div>
</template>

<script>
import 'vue-json-pretty/lib/styles.css'
import VueJsonPretty from 'vue-json-pretty'
import JsonEditor from 'vue-json-editor'
import {prepareDataForSave} from "@/utils/testcaseRequest";
import {interfaceApis} from "@/api/interface";
import {Message} from "element-ui";
import {validateTestCase} from "@/utils/testcaseValidator";
import Vue from "vue";
import {interfaceTestcaseApis} from "@/api/interface/interfaceTestcase";
import {fileApis} from "@/api/file";
import ScriptEditor from "@/views/interface-test/components/scriptEditor.vue";
import {environmentVariableApis} from "@/api/config/environmentVariable";
import AutoGenerateCasesDialog from "@/views/interface-test/components/autoGenerateCasesDialog.vue";

export default {
    name: "InterfaceTestcaseEditor",
    components: {
        VueJsonPretty,
        JsonEditor,
        ScriptEditor,
        AutoGenerateCasesDialog
    },
    props: {
        mode: {
            type: String,
            required: true,
            validator: function (value) {
                // 确保传入的值是有效的
                return ['add', 'edit'].indexOf(value) !== -1;
            }
        }
    },
    data() {
        return {
            // 左侧用例列表数据
            pageLoading: true,
            isUploading: false,
            isSendingRequest: false,
            interfaceId: this.$route.query.interfaceId,
            host: '',
            testCases: [],
            interfacePath: '',
            // 环境选择
            currentEnvironment: null,
            currentTestCase: {},
            editingTestCaseName: false, // 是否处于编辑状态
            editingTestCaseNameValue: '', // 编辑输入框的临时值
            environments: [],
            generateDialogVisible: false,
            cachePath: null
        };
    },
    computed: {
        responseHeadersArray() {
            const headers = this.currentTestCase?.response.headers || {};
            return Object.entries(headers).map(([key, value]) => ({ key, value }));
        },
        queryParamNum() {
            return this.currentTestCase?.queryParams?.reduce((prev, curr) => {
                if (curr.enabled) {
                    return prev + 1
                }
                return prev
            }, 0) || 0
        },
        mainCardLoadingText() {
            if (this.pageLoading) {
                return '正在加载用例数据...'
            }
            if (this.isSendingRequest) {
                return '正在发送请求...';
            }
            return ''; // 默认无文案
        },
        hasPreScript() {
            // 检查脚本是否存在且去除空格后不为空
            return this.currentTestCase.preRequestScript && this.currentTestCase.preRequestScript.trim() !== '';
        },
        hasAssertionScript() {
            return this.currentTestCase.postRequestScript && this.currentTestCase.postRequestScript.trim() !== '';
        },
        consoleLogs() {
            // 1. 分别获取前置和后置脚本的执行结果
            const preResult = this.currentTestCase.response?.preExecutionResult;
            const postResult = this.currentTestCase.response?.postExecutionResult;

            let finalLogs = '';

            // 2. 处理前置脚本的日志和错误
            if (preResult) {
                // 先添加logs
                if (preResult.logs) {
                    finalLogs += '--- Pre-request Script ---\n' + preResult.logs.trim() + '\n';
                }
                // 再添加error
                if (preResult.error) {
                    finalLogs += '--- Pre-request Script Errors ---\n' + preResult.error.trim() + '\n';
                }
            }

            // 3. 处理后置脚本的日志和错误
            if (postResult) {
                // 如果前面已经有日志了，加一个分隔符
                if (finalLogs) {
                    finalLogs += '\n\n';
                }
                // 先添加logs
                if (postResult.logs) {
                    finalLogs += '--- Tests Script ---\n' + postResult.logs.trim() + '\n';
                }
                // 再添加error
                if (postResult.error) {
                    finalLogs += '--- Tests Script Errors ---\n' + postResult.error.trim() + '\n';
                }
            }

            // 4. 返回最终拼接好的、去除了首尾多余空白的字符串
            return finalLogs.trim();
        },
        testResultList() {
            const preResults = this.currentTestCase.response?.preExecutionResult?.results || [];
            const postResults = this.currentTestCase.response?.postExecutionResult?.results || [];

            // 为结果添加一个来源标记，方便UI区分
            const formattedPreResults = preResults.map(r => ({ ...r, source: 'Pre-Script' }));
            const formattedPostResults = postResults.map(r => ({ ...r, source: 'Tests' }));

            return [...formattedPreResults, ...formattedPostResults];
        },
        testResultsSummary() {
            const results = this.testResultList;
            if (!results || results.length === 0) {
                return { passed: 0, failed: 0, total: 0 };
            }

            const passed = results.filter(r => r.result).length;
            const total = results.length;

            return {
                passed: passed,
                failed: total - passed,
                total: total,
                allPassed: passed === total
            };
        },
        statusCodeColor() {
            const code = this.currentTestCase.response?.statusCode;
            if (!code) return '#909399'; // 默认灰色
            if (code >= 200 && code < 300) return '#67c23a'; // 成功绿色
            if (code >= 300 && code < 400) return '#e6a23c'; // 重定向橙色
            if (code >= 400 && code < 500) return '#f56c6c'; // 客户端错误红色
            if (code >= 500) return '#f56c6c'; // 服务器错误红色
            return '#909399';
        },
        responseTimeColor() {
            const time = this.currentTestCase.response?.responseTime;
            if (time === undefined || time === null) return '#909399'; // 默认灰色
            if (time < 100) return '#67c23a';  // 绿色
            if (time < 500) return '#e6a23c';  // 橙色
            return '#f56c6c'; // 红色
        },
    },
    created() {
        this.initializePage();
    },
    activated() {
        if (this.cachePath !== null && this.cachePath !== this.$route.fullPath) {
            this.initializePage()
        }
    },
    methods: {
        async initializePage() {
            this.cachePath = this.$route.fullPath
            this.pageLoading = true;
            this.testCases = []

            try {
                const { mode } = this
                const { interfaceId, testcaseId } = this.$route.query
                if (mode === 'add' && !interfaceId) {
                    this.$message.error("缺少接口ID，无法加载页面！");
                    this.cachePath = ''
                    this.goBack()
                    return;
                }
                if (mode === 'edit' && !testcaseId) {
                    this.$message.error("缺少用例ID，无法加载页面！");
                    this.cachePath = ''
                    this.goBack()
                    return;
                }

                const promises = [
                    environmentVariableApis.getUserEnvironmentVariable(),
                    mode === 'edit' ? interfaceTestcaseApis.getInterfaceTestcaseDetail(testcaseId) : Promise.resolve(null),
                ]

                const [envRes, testcaseRes] = await Promise.all(promises);
                this.environments = envRes.data.map(v => ({
                    id: v.id,
                    name: v.name,
                    variables: v.variables
                }));

                const finalInterfaceId = mode === 'add' ? interfaceId : testcaseRes.data.interfaceId;
                const interfaceRes = await interfaceApis.getInterfaceDetail(finalInterfaceId);
                this.interfacePath = interfaceRes.data.path;

                const caseData = mode === 'edit' ? testcaseRes.data : {}; // 新增模式用空对象作为基础
                const formattedCase = this._formatTestCaseData(caseData);

                if (mode === 'edit') {
                    this.host = testcaseRes.data.host;
                    this.currentEnvironment = testcaseRes.data.envId;
                    this.interfaceId = testcaseRes.data.interfaceId; // 顺便更新一下 interfaceId
                }
                this.testCases.push(formattedCase);
                this.selectTestCase(formattedCase);
            } catch (e) {
                if (e.code) {
                    Message.error(e.message);
                }
                this.cachePath = ''
                this.goBack()
            } finally {
                this.pageLoading = false
            }
        },

        _formatTestCaseData(apiData={}) {
            const newCase = this.createNewTestCaseData()

            Object.assign(newCase, {
                id: apiData.id || newCase.id,
                name: apiData.name || newCase.name,
                path: apiData.path || newCase.path,
                method: apiData.method || newCase.method,
                priority: apiData.priority || newCase.priority,
                pathParam: apiData.pathParam || newCase.pathParam,
                queryParams: apiData.queryParams || newCase.queryParams,
                headers: apiData.headers || newCase.headers,
                requestBodyType: apiData.requestBodyType || newCase.requestBodyType,
                preRequestScript: apiData.preRequestScript || newCase.preRequestScript,
                postRequestScript: apiData.postRequestScript || newCase.postRequestScript,
                formDataParams: (apiData.requestBody && apiData.requestBody.formDataParams) || newCase.formDataParams,
                urlEncodedParams: (apiData.requestBody && apiData.requestBody.urlEncodedParams) || newCase.urlEncodedParams,
                jsonBody: (apiData.requestBody && apiData.requestBody.jsonBody) || newCase.jsonBody,
            })
            return newCase
        },

        // 新增一个空的用例数据
        createNewTestCaseData() {
            return {
                id: Date.now(), // 使用时间戳作为临时ID
                name: "未命名用例",
                priority: 0,
                method: 'GET',
                path: this.interfacePath || '无法获取接口路径，请稍后重试',
                pathParam: '',
                queryParams: [],
                headers: [
                    {key: 'Cache-Control', value: 'no-cache', description: '', enabled: true},
                    {key: 'Accept', value: '*/*', description: '', enabled: true},
                    {key: 'Accept-Encoding', value: 'gzip, deflate, br', description: '', enabled: true},
                    {key: 'Connection', value: 'keep-alive', description: '', enabled: true},
                ],
                // Tab 控制
                requestTabsActive: 'params',
                responseTabsActive: 'body',
                requestBodyType: 'none',  // none, form-data, x-www-form-urlencoded, json
                formDataParams: [],        // form-data 类型的参数列表
                urlEncodedParams: [],      // x-www-form-urlencoded 类型的参数列表
                jsonBody: null,              // json 类型的文本内容
                preRequestScript: '',              // 前置脚本
                postRequestScript: 'lt.test("test statusCode and response.body.code is 200", () => {\n  lt.expect(lt.response.statusCode).to.be.equal(200)\n  lt.expect(lt.response.body.code).to.be.equal(200)\n})\n',        // 断言脚本
                // 响应数据
                response: {
                    body: null,
                    cookies: [],
                    headers: {},
                    statusCode: null,
                    responseTime: null,
                    preExecutionResult: null,
                    postExecutionResult: null
                },
                hasJsonFlag: true
            }
        },
        startEditName() {
            this.editingTestCaseNameValue = this.currentTestCase.name;
            this.editingTestCaseName = true;
            this.$nextTick(() => {
                if (this.$refs.nameInput) {
                    this.$refs.nameInput.focus();
                }
            });
        },
        finishEditName() {
            const newName = this.editingTestCaseNameValue.trim();
            if (newName) {
                // 更新当前用例名称
                this.currentTestCase.name = newName;

                // 同步左侧用例列表中的名称（找到对应用例并更新）
                const idx = this.testCases.findIndex(c => c.id === this.currentTestCase.id);
                if (idx !== -1) {
                    this.testCases[idx].name = newName;
                }
            }
            this.editingTestCaseName = false;
        },
        // 模拟发送请求
        async sendRequest() {
            if (!this.host) {
                Message.error('请填写Host')
                return
            }
            if (!this.currentTestCase.path || this.currentTestCase.path.includes('无法获取')) {
                Message.error('请重新获取接口请求地址')
                this.goBack()
                return
            }
            // const requestData = prepareDataForSave(this.currentTestCase)
            const requestData = prepareDataForSave({
                ...this.currentTestCase,
                host: this.host
            })
            if (requestData) {
                this.isSendingRequest = true
                try {
                    const {data} = await interfaceTestcaseApis.sendInterfaceTestcaseRequest({
                        ...requestData,
                        interfaceId: this.interfaceId
                    }, this.currentEnvironment)
                    this.currentTestCase.response = {
                        ...data.response,
                        postExecutionResult: data.postExecutionResult,
                        preExecutionResult: data.preExecutionResult
                    }
                } catch (e) {
                    if (e.code) {
                        Message.error(e.message)
                    }
                    this.currentTestCase.response =  {
                        body: null,
                        cookies: [],
                        headers: {},
                        statusCode: null,
                        responseTime: null,
                        preExecutionResult: null,
                        postExecutionResult: null
                    }
                } finally {
                    this.isSendingRequest = false
                    this.syncTableSelection()
                }
            }
        },
        syncTableSelection() {
            // 使用 $nextTick 确保DOM已经更新完毕
            this.$nextTick(() => {
                // 恢复 queryParams 表格的勾选
                if (this.currentTestCase.queryParams && this.$refs.paramsTable) {
                    this.currentTestCase.queryParams.forEach(row => {
                        // 如果数据模型中 enabled 为 true，就勾选它
                        this.$refs.paramsTable.toggleRowSelection(row, row.enabled);
                    });
                }

                // 恢复 headers 表格的勾选
                if (this.currentTestCase.headers && this.$refs.headersTable) {
                    this.currentTestCase.headers.forEach(row => {
                        this.$refs.headersTable.toggleRowSelection(row, row.enabled);
                    });
                }

                // 恢复 formDataParams 表格的勾选
                if (this.currentTestCase.requestBodyType === 'form-data' && this.currentTestCase.formDataParams && this.$refs.formDataTable) {
                    this.currentTestCase.formDataParams.forEach(row => {
                        this.$refs.formDataTable.toggleRowSelection(row, row.enabled);
                    });
                }

                // 恢复 urlEncodedParams 表格的勾选
                if (this.currentTestCase.requestBodyType === 'x-www-form-urlencoded' && this.currentTestCase.urlEncodedParams && this.$refs.urlEncodedTable) {
                    this.currentTestCase.urlEncodedParams.forEach(row => {
                        this.$refs.urlEncodedTable.toggleRowSelection(row, row.enabled);
                    });
                }
            });
        },

        // 选择一个用例
        selectTestCase(testCase) {
            this.currentTestCase = testCase;
            this.syncTableSelection()
        },
        // 新增用例
        addTestCase() {
            const newCase = this.createNewTestCaseData();
            this.testCases.push(newCase);
            this.selectTestCase(newCase);
        },
        // 删除用例
        deleteTestCase(caseId) {
            this.$confirm('确认删除此测试用例吗?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning'
            }).then(() => {
                this.testCases = this.testCases.filter(c => c.id !== caseId);
                // 如果删除的是当前选中的用例，则默认选中第一个或创建一个新的
                if (this.currentTestCase.id === caseId) {
                    if (this.testCases.length > 0) {
                        this.selectTestCase(this.testCases[0]);
                    } else {
                        this.currentTestCase = this.createNewTestCaseData();
                    }
                }
                this.$message({
                    type: 'success',
                    message: '删除成功!'
                });
            }).catch(() => {
                // 用户取消操作
            });
        },
        // 新增一个查询参数行
        addQueryParam() {
            const param = {
                key: "",
                value: "",
                description: "",
                enabled: true
            }
            this.currentTestCase.queryParams.push(param);
            this.$refs.paramsTable.toggleRowSelection(param, true)
        },
        // 删除一个查询参数行
        removeQueryParam(index) {
            this.currentTestCase.queryParams.splice(index, 1);
        },
        addHeader() {
            const header = {
                key: '',
                value: '',
                description: '',
                enabled: true,
            }
            this.currentTestCase.headers.push(header)
            this.$refs.headersTable.toggleRowSelection(header, true)
        },
        removeHeader(index) {
            if (this.currentTestCase.headers && this.currentTestCase.headers.length > index) {
                this.currentTestCase.headers.splice(index, 1);
            }
        },
        // 处理用户手动勾选数据行Checkbox时触发的事件
        handleParamsSelect(selection, row) {
            row.enabled = !row.enabled
            if (row.enabled) {
                this.currentTestCase.pathParam = ''
            }
        },
        handleParamsSelectAll(selection) {
            const selectionSet = new Set(selection);
            this.currentTestCase.queryParams.forEach(param => {
                param.enabled = selectionSet.has(param);
            });
            if (selectionSet) {
                this.currentTestCase.pathParam = ''
            }
        },
        handleHeaderSelect(selection, row) {
            row.enabled = !row.enabled
        },
        handleHeaderSelectAll(selection) {
            const selectionSet = new Set(selection);
            this.currentTestCase.headers.forEach(header => {
                header.enabled = selectionSet.has(header);
            });
        },
        // form-data
        addFormDataParam() {
            const formData = {
                key: '',
                value: '',
                type: 'text',
                fileUrl: '',
                description: '',
                enabled: true,
            }
            this.currentTestCase.formDataParams.push(formData);
            this.$refs.formDataTable.toggleRowSelection(formData, true)
        },
        removeFormDataParam(index) {
            this.currentTestCase.formDataParams.splice(index, 1);
        },
        handleFormDataSelect(selection, row) {
            row.enabled = !row.enabled;
        },
        handleFormDataSelectAll(selection) {
            const selectionSet = new Set(selection);
            this.currentTestCase.formDataParams.forEach(param => {
                param.enabled = selectionSet.has(param);
            });
        },
        onFormDataTypeChange(row) {
            row.value = ''
            row.fileUrl = ''
        },
        onBeforeUpload(file) {
            const isLt10M = file.size / 1024 / 1024 < 10;
            if (!isLt10M) {
              this.$message.error('上传文件大小不能超过 10MB!');
              return false;
            }
            return isLt10M;
        },
        async handleFileUpload(options, row, index) {
            this.isUploading = true;
            try {
                // 调用后端接口上传文件
                const { data: ossUrl } = await fileApis.uploadTestcaseFile(options.file);
                // 上传成功
                row.value = options.file.name; // 更新UI，显示文件名
                row.fileUrl = ossUrl; // 存储返回的URL
                this.$message.success(`文件 '${options.file.name}' 上传成功！`);
            } catch (error) {
                this.$message.error(`文件 '${options.file.name}' 上传失败，请重试！`);
                // 上传失败，清空选择
                this.onRemoveFile(row);
            } finally {
                const uploadComponent = this.$refs[`uploadRef_${index}`]
                if (uploadComponent) {
                    uploadComponent.clearFiles()
                }
                this.isUploading = false;
            }
        },
        onRemoveFile(row) {
            row.value = '';
            row.fileUrl = ''
        },

        // x-www-form-urlencoded
        addUrlEncodedParam() {
            const urlEncodedParam = {
                key: '',
                value: '',
                description: '',
                enabled: true,
            }
            this.currentTestCase.urlEncodedParams.push(urlEncodedParam);
            this.$refs.urlEncodedTable.toggleRowSelection(urlEncodedParam, true)
        },
        removeUrlEncodedParam(index) {
            this.currentTestCase.urlEncodedParams.splice(index, 1);
        },
        handleUrlEncodedSelect(selection, row) {
            row.enabled = !row.enabled;
        },
        handleUrlEncodedSelectAll(selection) {
            const selectionSet = new Set(selection);
            this.currentTestCase.urlEncodedParams.forEach(param => {
                param.enabled = selectionSet.has(param);
            });
        },
        handleRadioChange(label) {
            // 将当前选中的testcase中body中form-data为enabled的表单参数自动勾选上
            if (this.currentTestCase.formDataParams && label === 'form-data') {
                this.currentTestCase.formDataParams.forEach(formData => {
                    if (formData.enabled) {
                        this.$nextTick(() => {
                            this.$refs.formDataTable.toggleRowSelection(formData, true)
                        })
                    }
                })
            }
            // 将当前选中的testcase中body中x-www-form-urlencoded为enabled的表单参数自动勾选上
            if (this.currentTestCase.urlEncodedParams && label === 'x-www-form-urlencoded') {
                this.currentTestCase.urlEncodedParams.forEach(formData => {
                    if (formData.enabled) {
                        this.$nextTick(() => {
                            this.$refs.urlEncodedTable.toggleRowSelection(formData, true)
                        })
                    }
                })
            }
        },
        onJsonChange(value) {
            // 实时保存
            this.onJsonSave(value)
        },
        onJsonSave(value) {
            this.currentTestCase.jsonBody = value
            this.currentTestCase.hasJsonFlag = true
        },
        onError() {
            this.currentTestCase.hasJsonFlag = false
        },
        goBack() {
            const path = this.$route.path
            this.bus.$emit('closeTagsView', {
                path,
                redirect: '/interfaces-test/testcaseList'
            });
            this.bus.$emit('reFreshTestcaseList')
        },
        async saveOrUpdateTestcases() {
            if (!this.testCases) {
                Message.error('当前用例为空')
                return
            }
            let allValid = true;

            // 1. 遍历所有用例，进行校验并更新错误状态
            this.testCases.forEach(testCase => {
                testCase.host = this.host
                testCase.envId = this.currentEnvironment
                // 校验时传入 interfaceId
                const errors = validateTestCase(this.mode, testCase, this.interfaceId);
                Vue.set(testCase, 'errors', errors);
                if (errors.length > 0) {
                    allValid = false;
                }
            });

            // 2. 根据校验结果决定后续操作
            if (allValid) {
                const saveData = this.testCases.map(tc => {
                    const requestData = prepareDataForSave(tc)
                    if (this.mode === 'add') {
                        delete requestData.id
                    }
                    return requestData
                });
                this.isSendingRequest = true
                try {
                    await interfaceTestcaseApis.saveInterfaceTestcases({
                        interfaceId: this.interfaceId,
                        interfaceTestcases: saveData
                    })
                    Message.success("保存成功")
                    this.goBack()
                }catch (e) {
                    if (e.code) {
                        Message.error(e.message)
                    }
                } finally {
                    this.isSendingRequest = false
                    this.syncTableSelection()
                }
            } else {
                Message.error("部分用例存在问题，请检查带有红色感叹号的用例。");
            }
        },
        handleGeneratedCases(newCases) {
            if (!newCases || newCases.length === 0) {
                this.$message.info("没有生成任何用例。");
                return;
            }

            this.$confirm(`即将为您生成 ${newCases.length} 个新的测试用例，是否继续？`, '确认', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'info'
            }).then(() => {
                this.testCases.push(...newCases);

                // 自动选中第一个新生成的用例
                this.selectTestCase(this.testCases[this.testCases.length - newCases.length]);
                this.$message.success(`成功生成 ${newCases.length} 个测试用例！`);
            }).catch(() => {});
        }
    },
    watch: {
        'currentTestCase.requestBodyType'(newVal) {
            // 过滤掉旧的 Content-Type
            // 找出 Content-Type 的索引
            const idx = this.currentTestCase.headers.findIndex(
                h => h.key.toLowerCase() === 'content-type'
            );

            if (idx !== -1) {
                this.currentTestCase.headers.splice(idx, 1);
            }
            let newHeader = null;
            switch (newVal) {
                case 'form-data':
                    newHeader = { key: 'Content-Type', value: 'multipart/form-data', description: '', enabled: true };
                    break;
                case 'x-www-form-urlencoded':
                    newHeader = { key: 'Content-Type', value: 'application/x-www-form-urlencoded', description: '', enabled: true };
                    break;
                case 'json':
                    newHeader = { key: 'Content-Type', value: 'application/json', description: '', enabled: true };
                    break;
            }
            if (newHeader) {
                this.currentTestCase.headers.push(newHeader);
                this.$nextTick(() => {
                    this.$refs.headersTable.toggleRowSelection(newHeader, true);
                })
            }
        },
        'currentTestCase.requestTabsActive'(newVal) {
            if (newVal === 'preRequestScript') {
                this.$nextTick(() => {
                    // 通过 ref 调用子组件的 refresh 方法
                    if (this.$refs.preRequestScript) {
                        this.$refs.preRequestScript.refresh();
                    }
                });
            }
            else if (newVal === 'postRequestScript') {
                this.$nextTick(() => {
                    // 通过 ref 调用子组件的 refresh 方法
                    if (this.$refs.postRequestScript) {
                        this.$refs.postRequestScript.refresh();
                    }
                });

            }
        }
    }
};
</script>

<style scoped>
/* 全局容器样式 */
.api-test-container {
    background-color: #f0f2f5;
    /*min-height: calc(100vh - 50px);*/
    height: calc(100vh - 50px);
}

/* Element UI 卡片样式覆盖 */
.box-card {
    border: 1px solid #e6e6e6;
    border-radius: 8px;
}

/* 左侧用例列表 */
.test-cases-card .el-card__header {
    background-color: #fafafa;
}

.test-case-list {
    max-height: 600px;
    overflow-y: auto;
}

.test-case-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 10px 12px;
    cursor: pointer;
    border-radius: 6px;
    transition: background-color 0.2s;
    margin-bottom: 5px;
    position: relative;
}

.test-case-item:hover {
    background-color: #ecf5ff;
}

.test-case-item.active {
    background-color: #d9ecff;
    color: #409eff;
    font-weight: bold;
}

.case-content {
    display: flex;
    align-items: center;
    /* 保证名称部分可以被压缩，为操作按钮留空间 */
    flex-grow: 1;
    overflow: hidden; /* 防止长名称溢出 */
}

.case-name {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.test-case-item:has(.error-icon) .case-name {
    padding-left: 20px; /* 为图标留出大约20px的空间 */
}


.test-case-item:hover {
    background-color: #ecf5ff;
}

.test-case-item.active {
    background-color: #d9ecff;
    color: #409eff;
    font-weight: bold;
}

.case-actions {
    visibility: hidden;
    /* 确保操作按钮不会被压缩 */
    flex-shrink: 0;
}

.test-case-item:hover .case-actions,
.test-case-item.active .case-actions {
    visibility: visible;
}

.case-actions .el-button {
    margin-left: 8px;
}

.error-icon {
    position: absolute;
    left: 12px; /* 距离左侧边缘的距离 */
    top: 50%; /* 垂直居中 */
    transform: translateY(-50%); /* 精确垂直居中 */
    color: #F56C6C !important;
    font-size: 14px;
}

.case-actions {
    visibility: hidden;
}

.test-case-item:hover .case-actions,
.test-case-item.active .case-actions {
    visibility: visible;
}

.case-actions .el-button {
    margin-left: 8px;
}

/* 右侧主区域 */
.top-bar {
    display: flex;
    align-items: center;
    justify-content: space-between; /* 关键点 */
    margin-bottom: 20px;
}

.error-text {
    color: #F56C6C;
    font-size: 15px;
    margin-left: 15px;
    margin-right: 5px;
    font-weight: normal;
    transition: all 0.3s ease;
}

.request-tabs {
    margin-top: 10px;
}

/* 响应区 */
.response-section {
    margin-top: 25px;
}

.section-title {
    margin-bottom: 10px;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}

.response-tabs .el-tabs__content {
    padding: 15px;
    background: #fff;
}

/* 底部按钮 */
.bottom-actions {
    margin-top: 10px;
    text-align: right;
}

/* 优化 el-input-group 外观 */
::v-deep .el-select .el-input {
    width: 110px;
}

::v-deep .el-input-group__prepend {
    background-color: #efeded;
}
.custom-env-select >>> {
    width: 160px !important;
}

.custom-env-select >>> .el-input {
    width: 160px !important;
}

.code-json-editor >>> .jsoneditor-poweredBy {
    display: none !important;
}

.code-json-editor >>> .jsoneditor-menu {
    display: none !important;
}

.code-json-editor >>> div.jsoneditor {
    border: 1px solid #dcdada;
}

.code-json-editor >>> div.jsoneditor-outer {
    height: 400px
}

/* 1. 主容器，使用flex布局 */
.url-bar {
    display: flex;
    align-items: center;
    gap: 8px; /* 控制各部分之间的间距 */
    width: 100%;
}

/* 2. method+host+path 的组合容器 */
.url-input-group {
    display: flex;
    flex-grow: 1; /* 让这个组合占据尽可能多的空间 */
}

/* 3. 对组合内的元素进行精细控制，去除边框圆角和相邻边框，使其看起来像一个整体 */

/* 方法选择器 (最左侧) */
.url-input-group >>> .method-select .el-input__inner {
    border-top-right-radius: 0;
    border-bottom-right-radius: 0;
}
.method-select {
    width: 110px;
    flex-shrink: 0; /* 防止被压缩 */
}

/* Host 输入框 (中间) */
.url-input-group >>> .host-input .el-input__inner {
    border-radius: 0; /* 中间元素没有圆角 */
    border-left-width: 0; /* 去掉左边框，与前一个元素合并 */
    border-right-width: 0; /* 去掉右边框，与后一个元素合并 */
}
.host-input {
    flex-grow: 1; /* 占据剩余空间 */
}

/* Path 输入框 (最右侧) */
.url-input-group >>> .path-input .el-input__inner {
    border-top-left-radius: 0;
    border-bottom-left-radius: 0;
    border-left-width: 0; /* 去掉左边框 */
    color: #1f1f1f;
}
.path-input {
    flex-grow: 1; /* 占据剩余空间 */
}

/* 4. 路径参数和发送按钮的样式 */
.path-param-input {
    width: 300px; /* 根据需要调整宽度 */
    flex-shrink: 0;
}

.send-button {
    white-space: nowrap;
    width: 160px;
}

.path-input ::v-deep .el-input__inner:disabled,
.path-param-input ::v-deep .el-input__inner:disabled {
    background-color: #e9ecef;
    color: #8b8d91;
    -webkit-text-fill-color: #8b8d91;
    border-color: #dcdfe6;
    cursor: not-allowed;
}

.empty-response-placeholder {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    min-height: 250px; /* 确保有一个最小高度，防止塌陷 */
    padding: 20px;
    text-align: center;
    color: #909399; /* 使用柔和的灰色 */
}

.empty-icon-box {
    font-size: 60px;
    line-height: 1;
    color: #c0c4cc;
    margin-bottom: 20px;
}

.empty-text-title {
    font-size: 16px;
    font-weight: 500;
    color: #606266;
    margin: 0 0 8px 0;
}

.empty-text-subtitle {
    font-size: 14px;
}

.loading-card {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
}
.tab-label-content {
    position: relative;
    display: inline-block; /* 让她表现得像一个块级元素，以便计算宽度和应用定位 */
    padding-right: 15px;  /* 给小绿点留出空间，防止文字和点重叠 */
}
.script-indicator-dot {
    position: absolute;
    top: 50%; /* 垂直居中 */
    right: 0; /* 定位到容器的最右边 */
    transform: translateY(-50%); /* 精确垂直居中 */
    width: 8px;
    height: 8px;
    background-color: #67c23a;
    border-radius: 50%;
    animation: blink 1.5s infinite ease-in-out;
}

/* 闪烁动画 (可选) */
@keyframes blink {
    0%, 100% {
        opacity: 1;
    }
    50% {
        opacity: 0.4;
    }
}

/* 响应区的标题栏，使用flex布局 */
.section-title-bar {
    display: flex;
    justify-content: space-between; /* 核心：让子元素两端对齐 */
    align-items: center;
    margin-bottom: 10px; /* 标题栏和Tabs之间的间距 */
}

/* 响应区的标题，移除默认的margin-bottom */
.section-title {
    margin: 0;
    font-size: 16px;
    font-weight: 600;
    color: #303133;
}

.response-meta-info {
    display: flex;
    align-items: center;
    gap: 20px; /* code和timeout之间的间距 */
    font-size: 14px;
    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace; /* 使用等宽字体，效果更佳 */
}

.meta-item {
    display: inline-flex;  /* 使用 inline-flex，让它表现得像行内元素但拥有flex能力 */
    align-items: baseline; /* 核心：让内部元素的文字基线对齐 */
    gap: 6px;           /* 标签和值之间的间距 */
}

/* 元信息的标签文字（'Code:', 'Time:'） */
.meta-label {
    color: #606266;
}

.meta-value {
    font-weight: bold; /* 只给值设置粗体 */
}

/* 元信息的值（状态码、耗时） */
.meta-item span:last-child {
    font-weight: bold;
    font-family: 'Courier New', Courier, monospace; /* 让数字更好看 */
}

/* 响应区Tabs的内边距可以适当减小 */
.response-tabs >>> .el-tabs__content {
    padding: 15px;
}

/* 控制台输出样式 */
.console-output-wrapper {
    background-color: #2d2d2d;
    color: #f0f0f0;
    padding: 15px;
    border-radius: 4px;
    font-family: 'Courier New', Courier, monospace;
    font-size: 14px;
    min-height: 250px;
    overflow-x: auto; /* 内容过长时可以横向滚动 */
}

.console-output-wrapper pre {
    margin: 0;
    white-space: pre-wrap;
    word-break: break-all;
}

.all-passed {
    color: #67c23a; /* 成功绿色 */
    font-weight: bold;
    margin-left: 5px;
}

.has-failures {
    color: #f56c6c; /* 失败红色 */
    font-weight: bold;
    margin-left: 5px;
}

.test-results-container {
    border: 1px solid #ebeef5;
    border-radius: 4px;
}

/* 测试结果列表项样式 */
.test-result-item {
    display: flex; /* 使用Flexbox布局 */
    padding: 12px 15px;
    border-bottom: 1px solid #ebeef5;
    align-items: flex-start; /* 顶部对齐 */
}
.test-result-item:last-child {
    border-bottom: none;
}

.test-result-header {
    display: flex;
    align-items: center; /* 垂直居中对齐 */
    gap: 10px;          /* 测试名和标签之间的间距 */
}

.script-source-tag {
    font-weight: normal;
}

/* 左侧状态标签的容器 */
.result-status-wrapper {
    flex-shrink: 0; /* 防止被压缩 */
    margin-right: 15px;
}

.result-status {
    display: inline-block;
    padding: 2px 8px;
    border-radius: 4px;
    color: #fff;
    font-size: 12px;
    font-weight: bold;
    min-width: 50px;
    text-align: center;
}

/* PASS 标签的颜色 */
.status-passed {
    background-color: #67c23a;
}

/* FAIL 标签的颜色 */
.status-failed {
    background-color: #f56c6c;
}

.test-result-content {
    flex-grow: 1;
    display: flex;
    flex-direction: column;
}

.test-name {
    color: #303133;
    font-size: 14px;
    font-weight: 500;
}

.script-source-tag {
    font-weight: normal;
}

.test-message-wrapper {
    margin-top: 8px;
    /* 让容器不占满整行，宽度由内容决定 */
    display: inline-block;
}

.test-message {
    display: block;
    margin: 0;
    padding: 8px 12px; /* 调整内边距 */
    background-color: #fef0f0;
    color: #c45656;
    border-radius: 4px;
    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, Courier, monospace;
    font-size: 13px;
    white-space: pre-wrap;
    word-break: break-all;
    border-left: 3px solid #f56c6c;
}

/* 通用的空状态占位符样式 */
.empty-placeholder {
    color: #909399;
    text-align: center;
    padding: 40px 0;
    font-size: 14px;
}
</style>
