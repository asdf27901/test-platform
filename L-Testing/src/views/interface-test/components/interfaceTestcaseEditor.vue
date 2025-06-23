<template>
    <div class="api-test-container">
        <el-row :gutter="8" v-if="!pageLoading">
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
                <el-card class="box-card" shadow="never">
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
                                    :key="item.value"
                                    :label="item.label"
                                    :value="item.value"
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
                            <el-input v-model="currentTestCase.host" placeholder="请输入Host，没有填写protocol，默认使用https" class="host-input"/>
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
                                                :file-list="scope.row.file ? [scope.row.file] : []"
                                                :auto-upload="false"
                                                :show-file-list="true"
                                                :on-remove="file => onRemoveFile(scope.row)"
                                                :before-upload="file => onBeforeUpload(file, scope.row)"
                                                :on-change="file => onFileChange(file, scope.row)"
                                                :multiple="false"
                                                :limit="1"
                                                :action="''"
                                                :http-request="() => {}"
                                                list-type="text"
                                            >
                                                <el-button size="small" type="primary">选择文件</el-button>
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

                            <div v-if="currentTestCase.requestBodyType === 'json'" class="code-json-editor">
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
                    </el-tabs>

                    <!-- 响应区 -->
                    <div class="response-section">
                        <h3 class="section-title">响应</h3>
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
                                    <el-table-column prop="expires" label="Expires" align="center"/>
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
                                    <el-table-column prop="value" label="Value" />
                                </el-table>
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
                 element-loading-text="正在加载用例数据..."
                 element-loading-spinner="el-icon-loading">
        </el-card>
    </div>
</template>

<script>
import {prepareDataForSave} from "@/utils/testcaseRequest";
import {interfaceApis} from "@/api/interface";
import {Message} from "element-ui";
import {validateTestCase} from "@/utils/testcaseValidator";
import Vue from "vue";
import {interfaceTestcaseApis} from "@/api/interface/testcase";

export default {
    name: "InterfaceTestcaseEditor",
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
            interfaceId: this.$route.query.interfaceId,
            testCases: [],
            interfacePath: '',
            currentTestCase: {},
            editingTestCaseName: false, // 是否处于编辑状态
            editingTestCaseNameValue: '', // 编辑输入框的临时值
            // 环境选择
            currentEnvironment: '',
            environments: [
                {label: '开发环境 (Dev)', value: 'dev'},
                {label: '测试环境 (Test)', value: 'test'},
                {label: '生产环境 (Prod)', value: 'prod'},
            ],
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
        }
    },
    created() {
        this.initializePage();
    },
    methods: {
        async initializePage() {
            this.pageLoading = true;
            if (this.mode === 'add') {
                try {
                    const interfaceId = this.$route.query.interfaceId;
                    if (!interfaceId) {
                        this.$message.error("缺少接口ID，无法加载页面！");
                        this.goBack()
                        return;
                    }

                    const {data} = await interfaceApis.getInterfaceDetail(interfaceId)
                    this.interfacePath = data.path

                } catch (error) {
                    if (error.code) {
                        Message.error(error.message)
                    }
                }finally {
                    // 判断加载后的逻辑
                    if (this.testCases.length > 0) {
                        // 如果有已有用例，默认选中第一个
                        this.selectTestCase(this.testCases[0]);
                    } else {
                        // 如果没有已有用例，创建一个新的默认用例
                        const newCase = this.createNewTestCaseData();
                        newCase.path = this.interfacePath || '无法获取接口路径，请稍后重试'
                        this.testCases.push(newCase);
                        this.selectTestCase(newCase);
                    }
                    this.pageLoading = false;
                }
            } else if (this.mode === 'edit') {
                const testcaseId = this.$route.query.testcaseId;
                if (!testcaseId) {
                    this.$message.error("缺少用例ID，无法加载页面！");
                    this.goBack()
                    return;
                }
                try {
                    const {data} = await interfaceTestcaseApis.getInterfaceTestcaseDetail(testcaseId)
                    this.interfaceId = data.interfaceId
                    const res =  await interfaceApis.getInterfaceDetail(this.interfaceId)
                    this.interfacePath = res.data.path
                    const response = {
                        body: null,
                        cookies: [],
                        headers: {},
                    }
                    this.currentTestCase = {
                        ...data,
                        ...data.requestBody,
                        response,
                        hasJsonFlag: true,
                        requestTabsActive: 'params',
                        responseTabsActive: 'body',
                        path: this.interfacePath
                    }
                    delete this.currentTestCase.requestBody
                } catch (e) {
                    if (e.code) {
                        Message.error(e.message)
                    }
                } finally {
                    this.testCases.push(this.currentTestCase)
                    this.selectTestCase(this.currentTestCase)
                    this.pageLoading = false
                }
            }
        },

        // 新增一个空的用例数据
        createNewTestCaseData() {
            return {
                id: Date.now(), // 使用时间戳作为临时ID
                name: "未命名用例",
                priority: 0,
                method: 'GET',
                host: '',
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
                // 响应数据
                response: {
                    body: null,
                    cookies: [],
                    headers: {},
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

        },

        // 选择一个用例
        selectTestCase(testCase) {
            this.currentTestCase = testCase;
            // 将当前选中的testcase中请求参数为enabled的请求参数自动勾选上
            if (this.currentTestCase.queryParams) {
                this.currentTestCase.queryParams.forEach(param => {
                    if (param.enabled) {
                        this.$nextTick(() => {
                            this.$refs.paramsTable.toggleRowSelection(param, true)
                        })
                    }
                })
            }
            // 将当前选中的testcase中headers为enabled的headers自动勾选上
            if (this.currentTestCase.headers) {
                this.currentTestCase.headers.forEach(header => {
                    if (header.enabled) {
                        this.$nextTick(() => {
                            this.$refs.headersTable.toggleRowSelection(header, true)
                        })
                    }
                })
            }
            // 将当前选中的testcase中body中form-data为enabled的表单参数自动勾选上
            if (this.currentTestCase.formDataParams && this.currentTestCase.requestBodyType === 'form-data') {
                this.currentTestCase.formDataParams.forEach(formData => {
                    if (formData.enabled) {
                        this.$nextTick(() => {
                            this.$refs.formDataTable.toggleRowSelection(formData, true)
                        })
                    }
                })
            }
            // 将当前选中的testcase中body中x-www-form-urlencoded为enabled的表单参数自动勾选上
            if (this.currentTestCase.urlEncodedParams && this.currentTestCase.requestBodyType === 'x-www-form-urlencoded') {
                this.currentTestCase.urlEncodedParams.forEach(formData => {
                    if (formData.enabled) {
                        this.$nextTick(() => {
                            this.$refs.urlEncodedTable.toggleRowSelection(formData, true)
                        })
                    }
                })
            }
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
        handleHeaderSelect(selection, row) {
            row.enabled = !row.enabled
        },
        // form-data
        addFormDataParam() {
            const formData = {
                key: '',
                value: '',
                type: 'text',
                file: null,
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
        onFormDataTypeChange(row) {
            if (row.type === 'text') {
                row.file = null;
            } else if (row.type === 'file') {
                row.value = '';
            }
        },
        onBeforeUpload(file, row) {
            row.file = file;
            row.value = file.name;
            return false;
        },
        onFileChange(file, row) {
            row.file = file.raw || file;
            row.value = row.file.name;
        },
        onRemoveFile(row) {
            row.file = null;
            row.value = '';
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
            this.bus.$emit('onCurrentContextmenuClick', {
                id: 1,
                path,
                redirect: '/interfaces-test/testcaseList'
            });
            this.bus.$emit('reFreshTestcaseList')
        },
        async saveOrUpdateTestcases() {
            let allValid = true;

            // 1. 遍历所有用例，进行校验并更新错误状态
            this.testCases.forEach(testCase => {
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
                }
            } else {
                Message.error("部分用例存在问题，请检查带有红色感叹号的用例。");
            }
        },
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
</style>
