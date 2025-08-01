<template>
    <el-dialog
            :title="title"
            :visible.sync="dialogVisible"
            width="500px"
            @close="handleClose"
    >
        <el-alert
            title="请注意：执行链路仅使用环境变量，不会修改或持久化任何对变量的更改。"
            type="warning"
            :closable="false"
            show-icon
            style="margin-bottom: 22px;"
        >
        </el-alert>
        <el-form ref="form" :model="form" label-width="90px" label-position="left">
            <el-form-item label="执行方式: " prop="executionMode" :required="true">
                <el-radio-group v-model="form.executionMode">
                    <el-radio label="case">使用用例自身绑定的环境</el-radio>
                    <el-radio label="global">全局环境</el-radio>
                </el-radio-group>
            </el-form-item>
            <el-form-item v-if="form.executionMode === 'global'" label="选择环境" prop="envId" :required="form.executionMode === 'global'" >
                <el-select v-model="form.envId" placeholder="请选择执行环境" style="width: 100%;" filterable>
                    <el-option
                            v-for="env in environments"
                            :key="env.id"
                            :label="env.name"
                            :value="env.id"
                    ></el-option>
                </el-select>
            </el-form-item>
        </el-form>
        <span slot="footer" class="dialog-footer">
      <el-button @click="handleCancel">取 消</el-button>
      <el-button type="primary" @click="handleConfirm" :loading="loading">确 定</el-button>
    </span>
    </el-dialog>
</template>

<script>
import { Message } from 'element-ui';
import {environmentVariableApis} from "@/api/config/environmentVariable";

export default {
    name: 'ExecuteChainRequestDialog',
    props: {
        visible: {
            type: Boolean,
            default: false,
        },
        chain: {
            type: Object,
            default: () => null,
        },
        loading: {
            type: Boolean,
            default: false
        }
    },
    data() {
        return {
            form: {
                executionMode: 'case',
                envId: null,
            },
            environments: [],
        };
    },
    created() {
        this.fetchEnvironments()
    },
    computed: {
        dialogVisible: {
            get() {
                return this.visible;
            },
            set(val) {
                this.$emit('update:visible', val);
            },
        },
        title() {
            return this.chain ? `执行链路: ${this.chain.name}` : '执行链路';
        },
    },
    watch: {
        // 弹窗可见时，如果环境列表为空，则加载
        visible(newVal) {
            if (newVal) {
                if (this.environments.length === 0) {
                    this.fetchEnvironments();
                }
                // 重置表单状态
                this.form.executionMode = 'case';
                this.form.envId = null;
            }
        },
    },
    methods: {
        async fetchEnvironments() {
            try {
                const { data } = await environmentVariableApis.getUserEnvironmentVariable();
                this.environments = data;
            } catch (error) {
                if (error.id){
                    Message.error(error.message)
                }
            }
        },
        handleClose() {
            this.dialogVisible = false;
        },
        handleCancel() {
            this.handleClose();
        },
        handleConfirm() {
            if (this.form.executionMode === 'global' && !this.form.envId) {
                Message.error('请选择一个全局环境');
                return;
            }
            // 触发 execute 事件，并将参数传递给父组件
            this.$emit('execute', {
                chainId: this.chain.id,
                envId: this.form.executionMode === 'global' ? this.form.envId : null,
            });
        },
    },
};
</script>
