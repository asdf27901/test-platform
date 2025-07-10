<template>
    <div>
        <el-card v-loading="pageLoading">
            <div slot="header" class="clearfix">
                <span style="font-weight: bold; font-size: 18px;">环境变量配置</span>
            </div>

            <!-- 顶部操作栏 -->
            <div class="top-bar">
                <el-select
                        v-model="currentEnvironmentId"
                        placeholder="请选择一个环境"
                        @change="handleEnvironmentChange"
                        clearable
                        style="width: 250px; margin-right: 10px;"
                >
                    <el-option
                            v-for="env in environments"
                            :key="env.id"
                            :label="env.name"
                            :value="env.id"
                    >
                    </el-option>
                </el-select>
                <el-button type="primary" icon="el-icon-plus" @click="createNewEnvironment">新建环境</el-button>
                <el-button
                        type="danger"
                        icon="el-icon-delete"
                        @click="deleteEnvironment"
                        :disabled="!currentEnvironmentId"
                >删除当前环境</el-button
                >
            </div>

            <!-- 编辑表单区域 -->
            <el-form
                    v-if="showForm"
                    :model="activeEnvironment"
                    label-width="100px"
                    label-position="top"
                    style="margin-top: 20px;"
            >
                <el-form-item label="环境名称" required style="font-weight: bold">
                    <el-input v-model="activeEnvironment.name" placeholder="例如：开发环境、测试环境"></el-input>
                </el-form-item>

                <el-form-item label="环境变量" style="font-weight: bold">
                    <el-table :data="activeEnvironment.variables" border style="width: 100%">
                        <el-table-column label="变量名称" width="250">
                            <template slot-scope="scope">
                                <el-input v-model="scope.row.key" placeholder="例如：base_url"></el-input>
                            </template>
                        </el-table-column>

                        <el-table-column label="变量值">
                            <template slot-scope="scope">
                                <!-- 根据 scope.row.type 显示不同输入框 -->
                                <el-input
                                    v-if="scope.row.type === 'string'"
                                    v-model="scope.row.value"
                                    placeholder="字符串类型"
                                ></el-input>

                                <el-input-number
                                    v-if="scope.row.type === 'number'"
                                    v-model="scope.row.value"
                                    controls-position="right"
                                    style="width: 100%"
                                ></el-input-number>

                                <el-switch
                                    v-if="scope.row.type === 'boolean'"
                                    v-model="scope.row.value"
                                ></el-switch>

                                <!-- 对于JSON（数组或对象），可以使用文本域，并提示用户输入合法的JSON -->
                                <el-input
                                    v-if="scope.row.type === 'object'"
                                    type="textarea"
                                    v-model="scope.row.value"
                                    placeholder='请输入合法的JSON，例如 [1, 2] 或 {"a": 1}'
                                ></el-input>
                            </template>
                        </el-table-column>

                        <el-table-column label="类型" width="120">
                            <template slot-scope="scope">
                                <el-select v-model="scope.row.type" @change="handleTypeChange(scope.row)">
                                    <el-option label="String" value="string"></el-option>
                                    <el-option label="Number" value="number"></el-option>
                                    <el-option label="Boolean" value="boolean"></el-option>
                                    <el-option label="Object" value="object"></el-option>
                                </el-select>
                            </template>
                        </el-table-column>

                        <el-table-column label="备注" width="300">
                            <template slot-scope="scope">
                                <el-input v-model="scope.row.remark" placeholder="选填"></el-input>
                            </template>
                        </el-table-column>

                        <el-table-column label="操作" width="100" align="center">
                            <template slot-scope="scope">
                                <el-button
                                        type="danger"
                                        icon="el-icon-delete"
                                        circle
                                        @click="deleteVariableRow(scope.$index)"
                                ></el-button>
                            </template>
                        </el-table-column>
                    </el-table>
                    <el-button
                            style="margin-top: 10px; width: 100%; border-style: dashed;"
                            icon="el-icon-plus"
                            @click="addVariableRow"
                    >添加变量</el-button
                    >
                </el-form-item>

                <el-form-item>
                    <el-button type="primary" @click="saveEnvironment">保 存</el-button>
                </el-form-item>
            </el-form>

            <el-empty v-else description="请选择一个环境或新建环境"></el-empty>

        </el-card>
    </div>
</template>

<script>

import {environmentVariableApis} from "@/api/config/environmentVariable";
import {Message} from "element-ui";

export default {
    name: 'EnvironmentVariable',
    data() {
        return {
            pageLoading: false,
            environments: [], // 存储从后端获取的所有环境列表 {id, name}
            currentEnvironmentId: null, // 当前选中的环境ID
            activeEnvironment: { // 当前正在编辑的环境的完整数据
                id: null,
                name: '',
                variables: [], // 注意：前端这里是对象数组，提交时需要转为JSON字符串
            },
            showForm: false, // 是否显示编辑表单
        };
    },
    async created() {
        await this.fetchEnvironments()
        this.autoSelectEnvironment()
    },
    methods: {
        autoSelectEnvironment() {
            if (this.environments) {
                this.currentEnvironmentId = this.environments[0]?.id
                this.handleEnvironmentChange(this.currentEnvironmentId)
            }
        },
        /**
         * @description 获取当前用户的所有环境列表
         */
        async fetchEnvironments() {
            this.pageLoading = true
            try {
                const {data} = await environmentVariableApis.getUserEnvironmentVariable()
                this.environments = data.map(v => {
                    return {
                        id: v.id,
                        name: v.name,
                        variables: v.variables
                    }
                })
            } catch (e) {
                if (e.code) {
                    Message.error(e.message)
                }
            } finally {
                this.pageLoading = false
            }
        },

        /**
         * @description 当下拉框选择变化时，加载对应的环境详情
         */
        handleEnvironmentChange(id) {
            if (!id) {
                this.showForm = false;
                return;
            }
            this.activeEnvironment = this.environments.find(v => v.id === id)
            this.showForm = true;
        },

        /**
         * @description 点击“新建环境”按钮
         */
        createNewEnvironment() {
            this.currentEnvironmentId = null; // 清空下拉框的选中状态
            this.activeEnvironment = {
                id: null,
                name: '',
                variables: [{ key: '', value: '', remark: '' }], // 默认给一行
            };
            this.showForm = true;
        },

        /**
         * @description 添加一个新的变量行
         */
        addVariableRow() {
            this.activeEnvironment.variables.push({ key: '', value: '', remark: '' });
        },

        /**
         * @description 删除一个变量行
         * @param {number} index - 要删除的行的索引
         */
        deleteVariableRow(index) {
            this.activeEnvironment.variables.splice(index, 1);
        },

        /**
         * @description 保存环境（新建或更新）
         */
        async saveEnvironment() {
            if (!this.activeEnvironment.name.trim()) {
                this.$message.error('环境名称不能为空！');
                return;
            }

            const keyCounts = this.activeEnvironment.variables.reduce((pre, cur) => {
                pre[cur.key] = (pre[cur.key] || 0) + 1;
                return pre
            }, {})

            if (keyCounts) {
                const keys = Object.keys(keyCounts)
                for (let key of keys) {
                    if (keyCounts[key] > 1) {
                        Message.error(key + "重复，请修改")
                        return
                    }
                }
            }

            this.pageLoading = true
            try {
                if (this.activeEnvironment.id) {
                    // 更新操作
                    await environmentVariableApis.updateEnvironmentVariable(this.activeEnvironment)
                    this.$message.success('环境更新成功！');
                } else {
                    // 新建操作
                    const {data} = await environmentVariableApis.saveEnvironmentVariable(this.activeEnvironment)
                    this.$message.success('环境新建成功！')
                    this.currentEnvironmentId = data
                    // 刷新列表以同步名称变化
                    await this.fetchEnvironments()
                }
            } catch (e) {
                if (e.code) {
                    Message.error(e.message)
                }
            } finally {
                this.pageLoading = false
            }
        },

        /**
         * @description 删除当前选中的环境
         */
        deleteEnvironment() {
            if (!this.currentEnvironmentId) return;

            this.$confirm('此操作将永久删除该环境配置, 是否继续?', '警告', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }).then(async () => {
                this.pageLoading = true
                try {
                    await environmentVariableApis.deleteUserEnvironmentVariable(this.currentEnvironmentId)
                    this.$message.success('删除成功!');
                    // 删除成功后，默认选中第一个环境
                    await this.fetchEnvironments()
                    this.autoSelectEnvironment()
                } catch (e) {
                    if (e.code) {
                        Message.error(e.message)
                    }
                } finally {
                    this.pageLoading = false
                }
            }).catch(() => {})
        },
    },
};
</script>

<style scoped>
.top-bar {
    display: flex;
    align-items: center;
}
.clearfix:before,
.clearfix:after {
    display: table;
    content: "";
}
.clearfix:after {
    clear: both;
}
</style>
