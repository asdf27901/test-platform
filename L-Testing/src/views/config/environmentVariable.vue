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
                                <el-input v-model="scope.row.value" placeholder="例如：http://api.dev.com"></el-input>
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
