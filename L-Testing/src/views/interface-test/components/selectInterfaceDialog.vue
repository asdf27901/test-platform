<template>
    <el-dialog
            title="请选择接口"
            :visible.sync="dialogVisible"
            width="30%"
            :close-on-click-modal="false"
            @close="handleClose"
    >
        <el-form :model="form" ref="formRef">
            <el-form-item
                    label="选择接口"
                    prop="interfaceId"
                    :rules="[{ required: true, message: '请必须选择一个接口', trigger: 'change' }]"
            >
                <el-select
                        v-model="form.interfaceId"
                        placeholder="请选择要创建用例的接口"
                        style="width: 100%"
                        filterable
                        clearable
                >
                    <el-option
                            v-for="item in interfaceList"
                            :key="item.id"
                            :label="`[${item.method}] ${item.name} (${item.path})`"
                            :value="item.id"
                    >
                        <span style="float: left; color: #8492a6; font-size: 13px">{{ `[${item.method}]` }}</span>
                        <span style="float: right">{{ `${item.name} (${item.path})` }}</span>
                    </el-option>
                </el-select>
            </el-form-item>
        </el-form>

        <span slot="footer" class="dialog-footer">
      <el-button @click="handleClose">取 消</el-button>
      <el-button
              type="primary"
              @click="handleConfirm"
              :disabled="!form.interfaceId"
      >
        下一步
      </el-button>
    </span>
    </el-dialog>
</template>

<script>
// 假设你有一个获取所有接口列表的API

import {interfaceApis} from "@/api/interface";
import {Message} from "element-ui";

export default {
    name: 'SelectInterfaceDialog',
    props: {
        // 通过 .sync 修饰符控制显示/隐藏
        visible: {
            type: Boolean,
            default: false,
        },
    },
    data() {
        return {
            form: {
                interfaceId: null,
            },
            interfaceList: [], // 接口列表数据
        };
    },
    computed: {
        // 让 props 的 visible 与组件内的 dialogVisible 同步
        dialogVisible: {
            get() {
                return this.visible;
            },
            set(val) {
                this.$emit('update:visible', val);
            },
        },
    },
    watch: {
        // 当弹窗显示时，获取接口列表数据
        visible(newVal) {
            if (newVal) {
                this.fetchInterfaceList();
            }
        },
    },
    methods: {
        // 获取接口列表
        async fetchInterfaceList() {
            try {
                const { data } = await interfaceApis.getActiveInterfaceList()
                this.interfaceList = data
            } catch (error) {
                if (error.code) {
                    Message.error(error.message)
                }
            }
        },
        // 关闭弹窗
        handleClose() {
            this.$refs.formRef.resetFields();
            this.form.interfaceId = null;
            this.dialogVisible = false;
        },
        // 点击确认
        handleConfirm() {
            this.$refs.formRef.validate((valid) => {
                if (valid) {
                    // 触发一个 success 事件，并把选中的接口ID传给父组件
                    this.$emit('success', this.form.interfaceId);
                    this.handleClose();
                } else {
                    return false;
                }
            });
        },
    },
};
</script>

<style scoped>
.el-select-dropdown__item {
    display: flex;
    justify-content: space-between;
    align-items: center;
}
::v-deep .el-dialog__header {
    text-align: left;
}
</style>
