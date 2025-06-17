<template>
    <el-dialog
        title="创建接口"
        :visible.sync="dialogVisible"
        width="40%"
        :before-close="handleClose"
        @open="onOpen"
        :close-on-click-modal="false"
    >
        <el-form :model="createForm" ref="createForm" :rules="createFormRules">
            <!-- 动态表单项 -->
            <div v-for="(item, index) in createForm.interfaces" :key="index" class="dynamic-interface-item">
                <!-- 新增接口序号行 -->
                <div class="interface-header">
                    <strong>接口{{ index + 1 }}</strong>
                    <el-col :span="24" style="text-align: right; margin-top: -30px;">
                        <el-button
                            type="danger"
                            icon="el-icon-remove-outline"
                            circle
                            @click="removeInterfaceRow(index)"
                            v-if="createForm.interfaces.length > 1"
                        ></el-button>
                    </el-col>
                </div>
                <el-row :gutter="20">
                    <el-col :span="18">
                        <el-form-item
                            label="接口名称"
                            :prop="'interfaces.' + index + '.name'"
                            :rules="createFormRules.name"
                            label-width="80px"
                        >
                            <el-input v-model="item.name" placeholder="请输入接口名称" maxlength="20" show-word-limit clearable></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row :gutter="20" style="margin-top: 20px;">
                    <el-col :span="10">
                        <el-form-item
                            label="请求方法"
                            :prop="'interfaces.' + index + '.method'"
                            :rules="createFormRules.method"
                            label-width="80px"
                        >
                            <el-select v-model="item.method" placeholder="请选择方法" style="width: 100%;">
                                <el-option
                                    v-for="opt in methodOptions"
                                    :key="opt.value"
                                    :label="opt.label"
                                    :value="opt.value"
                                ></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row :gutter="20" style="margin-top: 20px;">
                    <el-col :span="17">
                        <el-form-item
                            label="接口路径"
                            :prop="'interfaces.' + index + '.path'"
                            :rules="createFormRules.path"
                            label-width="80px"
                        >
                            <el-input v-model="item.path" placeholder="请输入接口路径, 如: /api/user/list" clearable></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>

                <el-row :gutter="20" style="margin-top: 20px;">
                    <el-col :span="24">
                        <el-form-item
                            label="接口描述"
                            :prop="'interfaces.' + index + '.description'"
                            label-width="80px"
                        >
                            <el-input type="textarea" v-model="item.description" placeholder="请输入接口描述"></el-input>
                        </el-form-item>
                    </el-col>
                </el-row>
            </div>
            <!-- 操作按钮 -->
            <div style="margin-top: 10px;">
                <el-button type="primary" @click="addInterfaceRow" icon="el-icon-circle-plus-outline">
                    新增一个接口
                </el-button>
            </div>
        </el-form>
        <span slot="footer" class="dialog-footer">
            <el-button @click="handleClose">取 消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="isSubmitting">确 定</el-button>
        </span>
    </el-dialog>
</template>

<script>
import {interfaceApis} from "@/api/interface";
import {Message} from "element-ui";

export default {
    name: 'CreateInterfaceDialog',
    props: {
        visible: {
            type: Boolean,
            default: false,
        },
    },
    data() {
        return {
            isSubmitting: false,
            // 表单数据模型
            createForm: {
                interfaces: [
                    { name: '', method: '', path: '', description: '' }
                ]
            },
            // 表单验证规则
            createFormRules: {
                name: [
                    { required: true, message: '请输入接口名称', trigger: 'blur' },
                    { validator: this.validateUniqueName, trigger: 'blur' }
                ],
                method: [
                    { required: true, message: '请选择请求方法', trigger: 'change' }
                ],
                path: [
                    { required: true, message: '请输入接口路径', trigger: 'blur' },
                    { pattern: /^\/[a-zA-Z0-9/\-_:]+$/, message: '请输入合法的路径, 以 / 开头', trigger: 'blur' }
                ]
            },
            // 请求方法选项
            methodOptions: [
                {value: 'GET', label: 'GET'},
                {value: 'POST', label: 'POST'},
                {value: 'PUT', label: 'PUT'},
                {value: 'DELETE', label: 'DELETE'},
                {value: 'PATCH', label: 'PATCH'},
            ],
        };
    },
    computed: {
        // 使用 computed 属性和 .sync 修饰符进行双向绑定
        dialogVisible: {
            get() {
                return this.visible;
            },
            set(val) {
                // 当 dialogVisible 改变时，通知父组件更新
                this.$emit('update:visible', val);
            }
        }
    },
    methods: {
        // 弹窗打开时，重置表单
        onOpen() {
            this.createForm = {
                interfaces: [{ name: '', method: '', path: '', description: '' }]
            };
            this.$nextTick(() => {
                this.$refs.createForm.clearValidate();
            });
        },
        handleSubmit() {
            this.$refs.createForm.validate(async (valid) => {
                if (valid) {
                    this.isSubmitting = true;
                    try {
                        await interfaceApis.addInterfaceBatch(this.createForm.interfaces)
                        this.$message.success(`成功提交 ${this.createForm.interfaces.length} 个接口进行创建！`);
                        this.$emit('success'); // 通知父组件创建成功
                        this.handleClose();
                    } catch (error) {
                        if (error.code) {
                            Message.error(error.message)
                        }
                    } finally {
                        this.isSubmitting = false;
                    }
                } else {
                    this.$message.error('表单校验失败，请检查输入项！');
                    return false;
                }
            });
        },
        handleClose() {
            this.dialogVisible = false;
        },
        addInterfaceRow() {
            this.createForm.interfaces.push({
                name: '', method: '', path: '', description: ''
            });
        },
        removeInterfaceRow(index) {
            if (this.createForm.interfaces.length > 1) {
                this.createForm.interfaces.splice(index, 1);
            }
        },
        validateUniqueName(rule, value, callback) {
            if (!value) {
                return callback(); // 必填规则已经验证，空值这里放行
            }

            // 当前校验项索引，取自 rule.prop 的路径，如 'interfaces.0.name'
            // 取索引方便排除自查
            const prop = rule.field || rule.prop;
            const match = prop.match(/interfaces\.(\d+)\.name/);
            if (!match) {
                // 如果找不到索引，直接放行（或报错）
                return callback();
            }
            const currentIndex = Number(match[1]);

            // 检查是否有重复的接口名称（排除当前索引）
            const interfaces = this.createForm.interfaces;
            const duplicate = interfaces.some((item, idx) => idx !== currentIndex && item.name === value);

            if (duplicate) {
                callback(new Error('接口名称不能重复'));
            } else {
                callback();
            }
        },
    }
};
</script>

<style scoped>
.dynamic-interface-item {
    padding: 15px;
    border: 1px dashed #dcdfe6;
    border-radius: 4px;
    margin-bottom: 20px;
    position: relative;
}

.interface-header {
    font-weight: 600;
    font-size: 16px;
    color: #303133;
    margin-bottom: 10px;
}
</style>
