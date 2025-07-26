<template>
    <el-dialog
            title="编辑接口"
            :visible.sync="dialogVisible"
            width="40%"
            @close="handleClose"
            :close-on-click-modal="false"
    >
        <el-form :model="form" :rules="rules" ref="editForm" label-width="80px">
            <el-row :gutter="20" style="margin-bottom: 20px;">
                <el-col :span="18">
                    <el-form-item label="接口名称" prop="name">
                        <el-input v-model.trim="form.name" placeholder="请输入接口名称" maxlength="20" show-word-limit clearable></el-input>
                    </el-form-item>
                </el-col>
            </el-row>

            <el-row :gutter="20" style="margin-bottom: 20px;">
                <el-col :span="10">
                    <el-form-item label="请求方法" prop="method">
                        <el-select v-model="form.method" placeholder="请选择方法" style="width: 100%;">
                            <el-option
                                    v-for="item in methodOptions"
                                    :key="item.value"
                                    :label="item.label"
                                    :value="item.value"
                            >
                                {{ item.label }}
                            </el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
            </el-row>

            <el-row style="margin-bottom: 20px;">
                <el-col :span="17">
                    <el-form-item label="接口路径" prop="path">
                        <el-input v-model.trim="form.path" placeholder="请输入接口路径, 如: /api/user/list" clearable></el-input>
                    </el-form-item>
                </el-col>
            </el-row>

            <el-row>
                <el-col :span="24">
                    <el-form-item label="接口描述" prop="description">
                        <el-input
                                type="textarea"
                                :rows="3"
                                v-model="form.description"
                                placeholder="请输入接口描述"
                        ></el-input>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>

        <span slot="footer" class="dialog-footer">
            <el-button @click="handleClose">取 消</el-button>
            <el-button type="primary" @click="handleConfirm" :loading="loading">确 定</el-button>
        </span>
    </el-dialog>
</template>

<script>
import { interfaceApis } from '@/api/interface';
import { Message } from 'element-ui';

export default {
    name: 'EditInterfaceDialog',
    props: {
        // 控制弹窗显示/隐藏
        visible: {
            type: Boolean,
            required: true,
        },
        // 要编辑的接口数据
        interfaceData: {
            type: Object,
            default: () => ({}),
        },
    },
    data() {
        return {
            loading: false,
            // 表单数据模型
            form: {
                id: null,
                name: '',
                method: '',
                path: '',
                description: '',
            },
            // 表单验证规则
            rules: {
                name: [{ required: true, message: '请输入接口名称', trigger: 'blur' }],
                method: [{ required: true, message: '请选择请求方法', trigger: 'change' }],
                path: [
                    { required: true, message: '请输入接口路径', trigger: 'blur' },
                    { pattern: /^\/[a-zA-Z0-9/\-_:]*$/, message: '请输入合法的路径, 以 / 开头', trigger: 'blur' },
                ],
            },
            // 请求方法选项 (与父组件保持一致)
            methodOptions: [
                { value: 'GET', label: 'GET' },
                { value: 'POST', label: 'POST' },
                { value: 'PUT', label: 'PUT' },
                { value: 'DELETE', label: 'DELETE' },
                { value: 'PATCH', label: 'PATCH' },
            ],
        };
    },
    computed: {
        // 使用计算属性和 .sync 修饰符的推荐实践
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
        // 监听传入的 interfaceData，当它变化时，更新表单数据
        interfaceData: {
            handler(newData) {
                if (newData) {
                    // 使用 Object.assign 避免直接修改 prop
                    const { id, name, method, path, description } = newData;
                    this.form = Object.assign({}, this.form, { id, name, method, path, description });
                }
            },
            immediate: true, // 立即执行一次，确保初始加载时数据正确
            deep: true,      // 深度监听对象内部变化
        },
    },
    methods: {
        // 确认提交
        handleConfirm() {
            this.$refs.editForm.validate(async (valid) => {
                if (valid) {
                    this.loading = true;
                    try {
                        // **注意**: 您的 API 需要一个更新方法，这里假设为 updateInterface
                        await interfaceApis.updateInterface(this.form);
                        Message.success('更新成功！');
                        this.$emit('success'); // 通知父组件更新成功
                        this.handleClose();   // 关闭弹窗
                    } catch (error) {
                        if (error.code) {
                            Message.error(error.message);
                        }
                    } finally {
                        this.loading = false;
                    }
                }
            });
        },
        // 关闭弹窗
        handleClose() {
            this.$refs.editForm.resetFields(); // 重置表单验证和数据
            this.dialogVisible = false; // 通过计算属性关闭弹窗
        },
    },
};
</script>

<style scoped>
/* 可根据需要添加样式 */
</style>
