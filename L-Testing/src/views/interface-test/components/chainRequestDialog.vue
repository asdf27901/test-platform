<template>
    <el-dialog
        :title="title"
        :visible.sync="dialogVisible"
        width="70%"
    >
        <el-form :model="form" ref="form" label-width="100px" style="margin-bottom: 20px">
            <el-form-item label="链路名称" prop="name" :rules="[{ required: true, message: '请输入链路请求名称', trigger: 'blur' }]">
                <el-input v-model.trim="form.name" placeholder="请输入链路请求名称" show-word-limit maxlength="50" style="width: 800px" clearable></el-input>
            </el-form-item>
        </el-form>

        <div class="transfer-container">
            <!-- 左侧：接口列表 -->
            <div class="panel api-panel" v-loading="loadingApis">
                <div class="panel-header">
                    <span>接口列表</span>
                    <el-input
                        v-model="apiSearchKeyword"
                        class="search-input"
                        placeholder="搜索接口名称或ID"
                        size="mini"
                        clearable
                        prefix-icon="el-icon-search"
                    ></el-input>
                </div>
                <ul class="panel-body">
                    <li
                        v-for="api in filteredApiList"
                        :key="api.id"
                        :class="{ active: api.id === activeApiId }"
                        @click="handleApiClick(api)"
                    >
                        <span>{{ api.name }} (ID: {{ api.id }})</span>
                        <el-badge :value="interfaceAndCaseIds[api.id].length" class="item" type="primary" v-if="interfaceAndCaseIds[api.id] && interfaceAndCaseIds[api.id].length > 0"></el-badge>
                    </li>
                    <el-empty v-if="apiList.length > 0 && filteredApiList.length === 0" description="无匹配接口" :image-size="80"></el-empty>
                    <el-empty v-if="apiList.length === 0" description="暂无接口数据" :image-size="80"></el-empty>
                </ul>
            </div>

            <!-- 中间：用例列表 -->
            <div class="panel case-panel" v-loading="loadingCases">
                <div class="panel-header">选择用例</div>
                <div class="panel-body">
                    <el-checkbox-group v-model="checkedCaseIds" @change="handleCaseCheckChange">
                        <el-checkbox
                            v-for="c in caseList"
                            :label="c.id"
                            :key="c.id"
                            class="case-checkbox"
                        >
                            {{ c.name }} (ID: {{ c.id }})
                        </el-checkbox>
                    </el-checkbox-group>
                    <el-empty v-if="!activeApiId" description="请先选择接口" :image-size="80"></el-empty>
                    <el-empty v-if="activeApiId && caseList.length === 0" description="该接口下暂无用例" :image-size="80"></el-empty>
                </div>
            </div>

            <!-- 右侧：已选用例（可拖拽排序） -->
            <div class="panel selected-panel">
                <div class="panel-header">已选用例 ({{ selectedCases.length }}个)</div>
                <div class="panel-body">
                    <draggable
                        v-model="selectedCases"
                        class="draggable-list"
                        ghost-class="ghost"
                        animation="200"
                    >
                        <transition-group type="transition" name="flip-list">
                            <div
                                v-for="(element, index) in selectedCases"
                                :key="element.id"
                                class="list-group-item"
                            >
                                <span>{{ index + 1 }}. {{ element.name }} (ID: {{ element.id }})</span>
                                <i class="el-icon-close" @click="removeSelectedCase(index)"></i>
                            </div>
                        </transition-group>
                    </draggable>
                    <el-empty v-if="selectedCases.length === 0" description="从左侧选择用例" :image-size="80"></el-empty>
                </div>
            </div>
        </div>

        <span slot="footer" class="dialog-footer">
      <el-button @click="dialogVisible = false">取 消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确 定</el-button>
    </span>
    </el-dialog>
</template>

<script>
import draggable from 'vuedraggable';
import {interfaceApis} from "@/api/interface";
import {Message} from "element-ui";
import {interfaceTestcaseApis} from "@/api/interface/interfaceTestcase";

export default {
    name: 'ChainRequestDialog',
    components: {
        draggable,
    },
    props: {
        visible: { type: Boolean, default: false },
        title: { type: String, default: '新增链路' },
        initialData: { type: Object, default: () => null },
        submitLoading: { type: Boolean, default: false, required: true }
    },
    data() {
        return {
            form: {
                id: null,
                name: ''
            },
            apiSearchKeyword: '',
            apiList: [],
            loadingApis: false,
            activeApiId: null,
            caseList: [],
            loadingCases: false,
            checkedCaseIds: [],
            selectedCases: [],
            interfaceAndCaseIds: {}
        };
    },
    computed: {
        dialogVisible: {
            get() { return this.visible; },
            set(val) { this.$emit('update:visible', val); },
        },
        filteredApiList() {
            if (!this.apiSearchKeyword) {
                return this.apiList;
            }
            const keyword = this.apiSearchKeyword.trim().toLowerCase();
            return this.apiList.filter(api =>
                api.name.toLowerCase().includes(keyword) || String(api.id).includes(keyword)
            );
        }
    },
    watch: {
        async visible(newVal) {
            if (newVal) {
                this.reset();
                await this.fetchApiList();
                if (this.initialData) {
                    this.form.id = this.initialData.id;
                    this.form.name = this.initialData.name;
                    this.interfaceAndCaseIds = this.initialData.interfaceAndCaseIds
                    this.selectedCases = this.initialData.selectedCases
                }
            }
        }
    },
    methods: {
        reset() {
            this.$nextTick(() => {
                this.$refs.form.resetFields();
            });
            this.form = {
                id: null,
                name: ''
            };
            this.apiSearchKeyword = '';
            this.apiList = [];
            this.activeApiId = null;
            this.caseList = [];
            this.checkedCaseIds = [];
            this.selectedCases = [];
            this.interfaceAndCaseIds = {}
        },
        async fetchApiList() {
            this.loadingApis = true;
            try {
                const {data} = await interfaceApis.getActiveInterfaceList();
                this.apiList = data
            } catch (e) {
                if (e.code) {
                    Message.error(e.message)
                }
            } finally {
                this.loadingApis = false;
            }
        },
        handleApiClick(api) {
            if (this.activeApiId === api.id) return;
            this.activeApiId = api.id;
            this.fetchCaseList();
        },
        async fetchCaseList() {
            this.loadingCases = true;
            try {
                const {data} = await interfaceTestcaseApis.getInterfaceTestcaseById({interfaceId: this.activeApiId})
                this.caseList = data.map(c => ({
                    ...c,
                    apiId: this.activeApiId, // 关键：为每个用例附加其所属的apiId
                }));
                const selectedIds = this.selectedCases.map(c => c.id);
                this.checkedCaseIds = this.caseList.map(c => c.id).filter(id => selectedIds.includes(id));
            } catch (e) {
                if (e.code) {
                    Message.error(e.message)
                }
            } finally {
                this.loadingCases = false
            }
        },
        handleCaseCheckChange(checkedIds) {
            checkedIds.forEach(id => {
                if (!this.selectedCases.some(c => c.id === id)) {
                    const caseToAdd = this.caseList.find(c => c.id === id);
                    if (caseToAdd) {
                        this.selectedCases.push(caseToAdd);
                        const testcase = this.caseList.find(c => c.id === id);
                        if (!this.interfaceAndCaseIds[testcase.apiId]) {
                            this.interfaceAndCaseIds[testcase.apiId] = [caseToAdd.id]
                        } else {
                            this.interfaceAndCaseIds[testcase.apiId].push(caseToAdd.id)
                        }
                    }
                }
            });
            const caseIdsOnPage = this.caseList.map(c => c.id);
            caseIdsOnPage.forEach(id => {
                if (!checkedIds.includes(id) && this.selectedCases.some(c => c.id === id)) {
                    const indexToRemove = this.selectedCases.findIndex(c => c.id === id);
                    if (indexToRemove > -1) {
                        this.selectedCases.splice(indexToRemove, 1);
                        const testcase = this.caseList.find(c => c.id === id);
                        if (this.interfaceAndCaseIds[testcase.apiId]?.length > 0) {
                            const index = this.interfaceAndCaseIds[testcase.apiId].findIndex(c => c === id)
                            if (index > -1) {
                                this.interfaceAndCaseIds[testcase.apiId].splice(index, 1)
                                if (this.interfaceAndCaseIds[testcase.apiId].length === 0) delete this.interfaceAndCaseIds[testcase.apiId]
                            }
                        }
                    }
                }
            });
        },
        removeSelectedCase(index) {
            const removeCase = this.selectedCases[index]
            if (Object.hasOwn(this.interfaceAndCaseIds, removeCase.apiId) && this.interfaceAndCaseIds[removeCase.apiId]) {
                const caseIdList = this.interfaceAndCaseIds[removeCase.apiId]
                if (caseIdList?.length > 0) {
                    const i = caseIdList?.findIndex(id => id === removeCase.id)
                    if (i > -1) caseIdList.splice(i, 1)
                }
            }
            if (this.checkedCaseIds.length > 0) {
                const i = this.checkedCaseIds.findIndex(id => id === removeCase.id)
                if (i > -1) this.checkedCaseIds.splice(i, 1)
            }
            this.selectedCases.splice(index, 1)
        },
        handleSubmit() {
            this.$refs.form.validate(valid => {
                if (valid) {
                    if (this.selectedCases.length === 0) {
                        this.$message.error('请至少选择一个用例');
                        return;
                    }
                    const finalData = {
                        ...this.form,
                        caseIds: this.selectedCases.map(c => c.id),
                        interfaceAndCaseIds: this.interfaceAndCaseIds
                    };
                    this.$emit('submit', {mode: !this.initialData ? 'add' : 'edit', data: finalData});
                }
            });
        },
    },
};
</script>

<style scoped>
.transfer-container {
    display: flex;
    justify-content: space-between;
    border: 1px solid #EBEEF5;
    border-radius: 4px;
}
.panel {
    width: 33.33%;
    height: 400px;
    display: flex;
    flex-direction: column;
}
.panel + .panel {
    border-left: 1px solid #EBEEF5;
}
.panel-header {
    padding: 8px 15px;
    background: #F5F7FA;
    border-bottom: 1px solid #EBEEF5;
    font-weight: bold;
    color: #303133;
}
.api-panel .panel-header {
    display: flex;
    flex-direction: column;
    padding: 8px 0;
}
.api-panel .panel-header > span {
    padding: 0 15px 5px;
}
.api-panel .search-input {
    width: calc(100% - 30px);
    margin: 0 15px;
}
.panel-body {
    flex-grow: 1;
    overflow-y: auto;
    padding: 10px 0;
}
.api-panel .panel-body {
    padding: 0;
}
.api-panel ul {
    list-style: none;
    padding: 0;
    margin: 0;
}
.api-panel li {
    padding: 10px 15px;
    cursor: pointer;
    transition: background-color 0.2s;
    display: flex;
    justify-content: space-between;
    align-items: center;
}
.api-panel .item {
    margin-left: 10px;
    margin-top: 5px;
}
::v-deep .api-panel .item .el-badge__content {
    line-height: 16px;
}
.api-panel li:hover {
    background-color: #F5F7FA;
}
.api-panel li.active {
    background-color: #ecf5ff;
    color: #409EFF;
}
.case-panel .case-checkbox {
    display: block;
    margin: 5px 15px;
}
::v-deep .el-checkbox__label {
    line-height: 30px;
}
.draggable-list {
    min-height: 360px;
}
.list-group-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 8px 15px;
    margin: 5px;
    background-color: #F5F7FA;
    border: 1px solid #e9e9eb;
    border-radius: 3px;
    cursor: grab;
}
.list-group-item i {
    cursor: pointer;
    visibility: hidden;
}
.list-group-item:hover i {
    visibility: visible;
    color: #F56C6C;
}
.ghost {
    opacity: 0.5;
    background: #c8ebfb;
}
.flip-list-move {
    transition: transform 0.5s;
}
.el-empty {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-direction: column;
}
</style>
