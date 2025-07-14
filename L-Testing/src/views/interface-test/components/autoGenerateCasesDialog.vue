<template>
    <el-dialog
        title="自动生成测试用例"
        :visible.sync="dialogVisible"
        width="90%"
        :close-on-click-modal="false"
        @close="onClose"
        append-to-body
    >
        <el-alert
            title="功能限制说明"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 20px;"
        >
            <div class="alert-content">
                <div>1. <span class="highlight">不支持自动生成路径参数</span> (例如 /users/{id})，相关测试用例需在生成后手动添加。</div>
                <div>2. <span class="highlight">不支持自动生成 multipart/form-data 中的文件类型参数</span>，所有参数将作为文本处理。</div>
            </div>
        </el-alert>
        <div class="dialog-content">
            <el-form label-width="90px" size="small" label-position="left">
                <el-form-item label="MIME 类型">
                    <el-select
                        v-model="selectedMimeType"
                        placeholder="请选择 MIME 类型"
                        style="width: 250px"
                    >
                        <el-option label="无 (None)" value="none"></el-option>
                        <el-option label="application/json" value="application/json"></el-option>
                        <el-option label="multipart/form-data" value="multipart/form-data"></el-option>
                        <el-option label="application/x-www-form-urlencoded" value="application/x-www-form-urlencoded"></el-option>
                    </el-select>
                </el-form-item>

                <el-form-item label="请求体模板" v-if="selectedMimeType === 'application/json'">
                    <div class="editor-container">
                        <json-editor
                            v-model="bodyTemplateObj"
                            :show-btns="false"
                            mode="code"
                            :expanded-on-start="true"
                            lang="zh"
                            @json-change="onJsonChange"
                            @json-save="onJsonSave"
                            @has-error="onJsonError"
                        />
                    </div>
                </el-form-item>
            </el-form>

            <el-divider>变量定义</el-divider>
            <el-table :data="variables" style="width: 100%" border size="small">
                <el-table-column prop="key" label="变量名 / Body路径" width="250px" align="center">
                    <template slot-scope="scope">
                        <el-input v-model="scope.row.key" :placeholder="keyPlaceholder(scope.row)"></el-input>
                    </template>
                </el-table-column>
                <el-table-column prop="location" label="位置" width="140px" align="center">
                    <template slot-scope="scope">
                        <el-select v-model="scope.row.location">
                            <el-option
                                v-for="opt in locationOptions"
                                :key="opt.value"
                                :label="opt.label"
                                :value="opt.value"
                            ></el-option>
                        </el-select>
                    </template>
                </el-table-column>
                <el-table-column prop="type" label="类型" width="140px" align="center">
                    <template slot-scope="scope">
                        <el-select v-model="scope.row.type" @change="handleTypeChange(scope.row)">
                            <el-option label="String" value="string"></el-option>
                            <el-option label="Number" value="number"></el-option>
                            <el-option label="Boolean" value="boolean"></el-option>
                            <el-option label="Object" value="object"></el-option>
                        </el-select>
                    </template>
                </el-table-column>
                <el-table-column prop="required" label="必填" width="80" align="center">
                    <template slot-scope="scope">
                        <el-switch v-model="scope.row.required"></el-switch>
                    </template>
                </el-table-column>
                <el-table-column label="允许为null" width="90" align="center">
                    <template slot-scope="scope">
                        <el-switch v-model="scope.row.allowNull" :disabled="!scope.row.required"></el-switch>
                    </template>
                </el-table-column>
                <el-table-column label="限制字符长度" width="100" align="center">
                    <template slot-scope="scope">
                        <el-switch v-model="scope.row.limitLength" :disabled="scope.row.type !== 'string'"></el-switch>
                    </template>
                </el-table-column>
                <el-table-column label="允许空对象" width="90" align="center">
                    <template slot-scope="scope">
                        <el-switch v-model="scope.row.allowEmpty" :disabled="scope.row.type !== 'object'"></el-switch>
                    </template>
                </el-table-column>
                <el-table-column label="边界/默认值" align="center">
                    <template slot-scope="scope">
                        <div v-if="scope.row.type === 'number'" style="display: flex; gap: 10px;">
                            <el-input-number
                                v-model="scope.row.min"
                                placeholder="最小值，默认不限制"
                                size="middle"
                                :precision="0"
                                controls-position="right"
                            ></el-input-number>
                            <el-input-number
                                v-model="scope.row.max"
                                placeholder="最大值，默认不限制"
                                size="middle"
                                :precision="0"
                                :min="scope.row.min"
                                controls-position="right"
                            ></el-input-number>
                        </div>
                        <el-switch
                            v-else-if="scope.row.type === 'boolean'"
                            v-model="scope.row.defaultValue"
                            active-text="true"
                            inactive-text="false"
                        ></el-switch>
                        <el-input
                            v-else-if="scope.row.type === 'object'"
                            type="textarea"
                            :rows="2" autosize
                            v-model="scope.row.defaultValue"
                            placeholder='请输入合法的 JSON 对象字符串, 例如: {"key": "value"}'
                        ></el-input>
                        <div v-else>
                            <el-input v-model="scope.row.defaultValue" placeholder="输入一个合法的默认值" style="margin-bottom: 5px;"></el-input>
                            <div style="display: flex; gap: 10px;" v-if="scope.row.limitLength">
                                <el-input-number
                                    v-model="scope.row.minLength"
                                    placeholder="最小长度"
                                    size="middle"
                                    :min="0"
                                    :precision="0"
                                    controls-position="right"
                                ></el-input-number>
                                <el-input-number
                                    v-model="scope.row.maxLength"
                                    placeholder="最大长度，默认不限制"
                                    size="middle"
                                    :min="scope.row.minLength || 0"
                                    :precision="0"
                                    controls-position="right"
                                ></el-input-number>
                            </div>
                        </div>
                    </template>
                </el-table-column>
                <el-table-column label="操作" width="80" align="center">
                    <template slot-scope="scope">
                        <el-button type="danger" icon="el-icon-delete" circle size="mini" @click="removeVariable(scope.$index)"></el-button>
                    </template>
                </el-table-column>
            </el-table>
            <el-button icon="el-icon-plus" @click="addVariable" style="margin-top: 10px; width: 100%; border-style: dashed;">
                添加变量
            </el-button>
        </div>
        <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="handleGenerate" :disabled="selectedMimeType === 'application/json' && !isBodyTemplateValid">生 成</el-button>
    </span>
    </el-dialog>
</template>

<script>
import JsonEditor from 'vue-json-editor';
import {Message} from "element-ui";

function setValueByPath(obj, path, value) {
    const keys = path.split('.');
    let current = obj;
    for (let i = 0; i < keys.length - 1; i++) {
        const key = keys[i];
        if (!current[key] || typeof current[key] !== 'object') current[key] = {};
        current = current[key];
    }
    current[keys[keys.length - 1]] = value;
}
function removeValueByPath(obj, path) {
    const keys = path.split('.');
    let current = obj;
    for (let i = 0; i < keys.length - 1; i++) {
        const key = keys[i];
        if (!current[key]) return;
        current = current[key];
    }
    delete current[keys[keys.length - 1]];
}
const createNewVariable = () => ({
    key: '',
    location: 'query',
    type: 'string',
    required: true,
    allowNull: false,
    defaultValue: '',
    min: undefined,
    max: undefined,
    limitLength: false,
    allowEmpty: false,
    minLength: 0,
    maxLength: undefined
});

export default {
    name: "AutoGenerateCasesDialog",
    components: { JsonEditor },
    props: {
        visible: { type: Boolean, default: false },
        caseTplCreator: { type: Function, required: true }
    },
    data() {
        return {
            selectedMimeType: 'none',
            bodyTemplateObj: { id: 123, user: { name: "template_name" } },
            isBodyTemplateValid: true,
            variables: [createNewVariable()],
        };
    },
    computed: {
        dialogVisible: {
            get() { return this.visible; },
            set(val) { this.$emit('update:visible', val); }
        },
        locationOptions() {
            const options = [
                { label: 'Query', value: 'query' },
                { label: 'Header', value: 'header' },
            ];
            if (this.selectedMimeType === 'application/json') {
                options.push({ label: 'Body', value: 'body' });
            } else if (this.selectedMimeType === 'multipart/form-data' || this.selectedMimeType === 'application/x-www-form-urlencoded') {
                options.push({ label: 'Form', value: 'form' });
            }
            return options;
        }
    },
    watch: {
        selectedMimeType(newType) {
            this.variables.forEach(v => {
                if (v.location === 'body' && newType !== 'application/json') {
                    v.location = 'query';
                }
                if (v.location === 'form' && (newType !== 'multipart/form-data' && newType !== 'application/x-www-form-urlencoded')) {
                    v.location = 'query';
                }
            });
        },
        'variables': {
            handler(newVariables) {
                newVariables.forEach(v => {
                    if (!v.required) {
                        v.allowNull = false;
                    }
                    if (v.type === 'boolean' && typeof v.defaultValue !== 'boolean') {
                        v.defaultValue = !!v.defaultValue;
                    }
                });
            },
            deep: true
        }
    },
    methods: {
        onJsonChange(value) {
            // 实时保存
            this.onJsonSave(value)
        },
        onJsonSave(value) {
            this.bodyTemplateObj = value
            this.isBodyTemplateValid = true
        },
        onJsonError() {
            this.isBodyTemplateValid = false
        },
        keyPlaceholder(variable) {
            if (variable.location === 'body' || variable.location === 'form') {
                if (this.selectedMimeType === 'application/json') {
                    return '输入JSON路径, 如: user.name';
                } else if (this.selectedMimeType === 'multipart/form-data' || this.selectedMimeType === 'application/x-www-form-urlencoded') {
                    return '输入表单字段名称';
                }
            }
            return '输入参数名称';
        },
        handleTypeChange(row) {
            row.defaultValue = '';

            // 重置 Number 类型的边界值
            row.min = undefined;
            row.max = undefined;

            // 重置 String 类型的长度限制
            row.limitLength = false;
            row.allowEmpty = false
            row.minLength = 0;
            row.maxLength = undefined;
        },
        addVariable() { this.variables.push(createNewVariable()); },
        removeVariable(index) { this.variables.splice(index, 1); },
        onClose() {
            this.selectedMimeType = 'none';
            this.bodyTemplateObj = { id: 123, user: { name: "template_name" } };
            this.isBodyTemplateValid = true;
            this.variables = [createNewVariable()];
        },
        checkVariablesSetting() {
            for (const v of this.variables) {
                if (!v.key) {
                    Message.error("请为所有变量填写'变量名 / Body路径");
                    return false;
                }
                if (v.type === 'number') {
                    if (v.max) {
                        // 有上限
                        v.defaultValue = v.max
                    } else {
                        // 无上限
                        if (!v.min) {
                            // 无下限
                            v.defaultValue = 0
                        } else {
                            // 有下限
                            v.defaultValue = v.min + 1
                        }
                    }
                }
                if (v.defaultValue === '' || v.defaultValue === null || v.defaultValue === undefined) {
                    Message.error(`变量 "${v.key}" 的默认值不能为空`);
                    return false;
                }
            }
            return true
        },
        handleGenerate() {
            if (this.selectedMimeType === 'application/json' && !this.isBodyTemplateValid) {
                this.$message.error('请修正请求体模板中的JSON格式错误。');
                return;
            }
            // 判断当前的变量名和默认值是否已经设定
            if (!this.checkVariablesSetting()) {
                return
            }
            const generatedCases = [];
            const baseCase = this.caseTplCreator();
            baseCase.name = "【自动生成】成功用例 - 所有参数合法";
            baseCase.headers = baseCase.headers.filter(h => h.key.toLowerCase() !== 'content-type');
            switch (this.selectedMimeType) {
                case 'application/json':
                    baseCase.requestBodyType = 'json';
                    baseCase.jsonBody = JSON.parse(JSON.stringify(this.bodyTemplateObj));
                    baseCase.headers.push({ key: 'Content-Type', value: this.selectedMimeType, description: '', enabled: true });
                    break;
                case 'multipart/form-data':
                    baseCase.requestBodyType = 'form-data';
                    baseCase.headers.push({ key: 'Content-Type', value: this.selectedMimeType, description: '', enabled: true });
                    break;
                case 'application/x-www-form-urlencoded':
                    baseCase.requestBodyType = 'x-www-form-urlencoded';
                    baseCase.headers.push({ key: 'Content-Type', value: this.selectedMimeType, description: '', enabled: true });
                    break;
                default:
                    baseCase.requestBodyType = 'none';
            }
            for (const v of this.variables) {
                try {
                    this.setParameter(baseCase, v.location, v.key, v.defaultValue, v.type);
                } catch (e) {
                    Message.error(v.key + "默认值有误，请检查")
                    return
                }
            }
            generatedCases.push(baseCase);
            this.variables.forEach(variable => {
                // 参数不是必填
                if (!variable.required) {
                    const notRequiredParamCase = this.createCaseFromBase(baseCase, `【自动生成】缺少非必填参数: {${variable.key}}`)
                    this.removeParameter(notRequiredParamCase, variable.location, variable.key)
                    generatedCases.push(notRequiredParamCase)
                }
                // 必填参数不填
                if (variable.required) {
                    const missingParamCase = this.createCaseFromBase(baseCase, `【自动生成】缺少必填参数: {${variable.key}}`);
                    this.removeParameter(missingParamCase, variable.location, variable.key);
                    generatedCases.push(missingParamCase);
                }
                // 必填参数，允许为空
                if (variable.required && variable.allowNull) {
                    const nullParamCase = this.createCaseFromBase(baseCase, `【自动生成】必填参数{${variable.key}}为null`);
                    this.setParameter(nullParamCase, variable.location, variable.key, null, variable.type);
                    generatedCases.push(nullParamCase);
                }
                // 错误类型, 如果不是body的话不用考虑错误类型
                if (variable.location === 'body') {
                    const errorTypeCase = this.createCaseFromBase(baseCase, `【自动生成】参数{${variable.key}}为错误类型`)
                    this.setParameter(errorTypeCase, variable.location, variable.key, this.getWrongTypeValue(variable.type), variable.type, true)
                    generatedCases.push(errorTypeCase)
                }
                // 数值大小边界情况
                if (variable.type === 'number') {
                    const { min, max } = variable;
                    if (typeof min === 'number') {
                        const lowerBoundCase = this.createCaseFromBase(baseCase, `【自动生成】边界测试: {${variable.key}} < 最小值`);
                        this.setParameter(lowerBoundCase, variable.location, variable.key, min - 1, variable.type);
                        generatedCases.push(lowerBoundCase);
                    }
                    if (typeof max === 'number') {
                        const upperBoundCase = this.createCaseFromBase(baseCase, `【自动生成】边界测试: {${variable.key}} > 最大值`);
                        this.setParameter(upperBoundCase, variable.location, variable.key, max + 1, variable.type);
                        generatedCases.push(upperBoundCase);
                    }
                }
                // 字符串长度边界情况
                if (variable.type === 'string' && variable.limitLength) {
                    const { minLength, maxLength } = variable;
                    if (typeof minLength === 'number') {
                        const shorterStringCase = this.createCaseFromBase(baseCase, `【自动生成】长度测试: {${variable.key}} < 最小长度`);
                        if (minLength > 0) {
                            const shortString = 's'.repeat(minLength - 1);
                            this.setParameter(shorterStringCase, variable.location, variable.key, shortString, variable.type);
                        } else {
                            this.setParameter(shorterStringCase, variable.location, variable.key, null, variable.type);
                        }
                        generatedCases.push(shorterStringCase);
                    }
                    if (typeof maxLength === 'number') {
                        const longerStringCase = this.createCaseFromBase(baseCase, `【自动生成】长度测试: {${variable.key}} > 最大长度`);
                        const longString = 's'.repeat(maxLength + 1);
                        this.setParameter(longerStringCase, variable.location, variable.key, longString, variable.type);
                        generatedCases.push(longerStringCase);
                    }
                }
                if (variable.type === 'object' && variable.allowNull) {
                    const parseValue = JSON.parse(variable.defaultValue)
                    if (Array.isArray(parseValue)) {
                        const emptyObjectCase = this.createCaseFromBase(baseCase, `【自动生成】空对象测试: {${variable.key}}为空对象[]`)
                        this.setParameter(emptyObjectCase, variable.location, variable.key, [], variable.type, true);
                        generatedCases.push(emptyObjectCase)
                    } else if (typeof parseValue === 'object') {
                        const emptyObjectCase = this.createCaseFromBase(baseCase, `【自动生成】空对象测试: {${variable.key}}为空对象{}`)
                        this.setParameter(emptyObjectCase, variable.location, variable.key, {}, variable.type, true);
                        generatedCases.push(emptyObjectCase)
                    }
                }
            });
            this.$emit('generate', generatedCases);
            this.dialogVisible = false;
        },
        getWrongTypeValue(type) {
            switch (type) {
                case 'string': return 12345;
                case 'number': return 'not_a_number';
                case 'boolean': return 'not_a_boolean';
                case 'object': return 'not_an_object';
                default: return null;
            }
        },
        createCaseFromBase(baseCase, newName) {
            const newCase = JSON.parse(JSON.stringify(baseCase));
            newCase.id = Date.now() + Math.random();
            newCase.name = newName;
            return newCase;
        },
        setParameter(testCase, location, key, value, type, isObjectErrorCase=false) {
            if (!key) return;

            if (type === 'object' && !isObjectErrorCase) {
                const parsedValue = JSON.parse(value);
                if (location !== 'body' && Array.isArray(parsedValue)) {
                    value = parsedValue.join(',')
                } else if (location === 'body') {
                    value = parsedValue
                }
            } else if (type === 'boolean' && location !== 'body') {
                if (value === true) {
                    value = 'true'
                } else if (value === false) {
                    value = 'false'
                }
            }

            if (location === 'body') {
                if(testCase.jsonBody) {
                    setValueByPath(testCase.jsonBody, key, value);
                }
            }else if (location === 'form') {
                if (testCase.requestBodyType === 'form-data') {
                    const fdParam = testCase.formDataParams.find(p => p.key === key);
                    if (fdParam) {
                        fdParam.value = value;
                    } else {
                        testCase.formDataParams.push({ key, value, type: 'text', description: '', enabled: true });
                    }
                } else {
                    const ueParam = testCase.urlEncodedParams.find(p => p.key === key);
                    if (ueParam) {
                        ueParam.value = value;
                    } else {
                        testCase.urlEncodedParams.push({ key, value, description: '', enabled: true });
                    }
                }
            } else if (location === 'header') {
                const hParam = testCase.headers.find(p => p.key === key);
                if (hParam){
                    hParam.value = value;
                } else {
                    testCase.headers.push({ key, value, description: '', enabled: true });
                }
            } else { // query
                const qParam = testCase.queryParams.find(p => p.key === key);
                if (qParam){
                    qParam.value = value;
                } else {
                    testCase.queryParams.push({ key, value, description: '', enabled: true });
                }
            }
        },
        removeParameter(testCase, location, key) {
            if (!key) return;
            if (location === 'body') {
                if(testCase.jsonBody) removeValueByPath(testCase.jsonBody, key);
            } else if (location === 'form') {
                if (testCase.requestBodyType === 'form-data') {
                    testCase.formDataParams = testCase.formDataParams.filter(p => p.key !== key);
                } else {
                    testCase.urlEncodedParams = testCase.urlEncodedParams.filter(p => p.key !== key);
                }
            } else if (location === 'header') {
                testCase.headers = testCase.headers.filter(p => p.key !== key);
            } else { // query
                testCase.queryParams = testCase.queryParams.filter(p => p.key !== key);
            }
        }
    }
};
</script>

<style scoped>
.alert-content {
    line-height: 1.8;
    font-size: 13px;
}
.alert-content .highlight {
    color: #F56C6C; /* Element UI 的 Danger 红色 */
    font-weight: bold;
}
.dialog-content {
    max-height: 70vh;
    overflow-y: auto;
}
.el-input-number {
    width: 100%;
}
.el-select >>> .el-input {
    width: 100% !important;
}
.editor-container {
    border: 1px solid #DCDFE6;
    border-radius: 4px;
}
.editor-container >>> .jsoneditor-poweredBy,
.editor-container >>> div.jsoneditor-menu {
    display: none !important;
}
.editor-container >>> div.jsoneditor-outer {
    height: 200px;
}
.editor-container >>> div.jsoneditor {
    border: none;
}
.number-input >>> .el-input__inner {
    text-align: center;
}
</style>
