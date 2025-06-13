<template>
    <el-drawer
        :title="title"
        :visible.sync="drawerVisible"
        direction="rtl"
        size="500px"
        :wrapperClosable="true"
        :before-close="handleClose"
    >
        <!-- 主体内容区 -->
        <div class="drawer-content">
            <el-form ref="userForm" :model="form" :rules="rules" label-width="80px">
                <el-form-item label="用户名" prop="username">
                    <el-input
                        v-model="form.username"
                        placeholder="请输入用户名"
                        :disabled="isEditMode"
                        maxlength="10"
                        show-word-limit
                    ></el-input>
                </el-form-item>
                <el-form-item label="用户昵称" prop="nickName">
                    <el-input
                        v-model="form.nickName"
                        placeholder="请输入用户昵称"
                        maxlength="10"
                        show-word-limit
                    ></el-input>
                </el-form-item>
                <el-form-item label="密码" prop="password">
                    <el-input
                        type="password"
                        v-model="form.password"
                        :placeholder="passwordPlaceholder"
                    ></el-input>
                </el-form-item>
                <!-- 头像上传组件 -->
                <el-form-item label="用户头像" prop="avatarUrl">
                    <el-upload
                        class="avatar-uploader"
                        action="http://localhost:8080/file/uploadImage"
                        :headers="uploadHeaders"
                        :show-file-list="false"
                        :on-success="handleAvatarSuccess"
                        :before-upload="beforeAvatarUpload"
                    >
                        <img v-if="form.avatarUrl" :src="form.avatarUrl" class="avatar">
                        <i v-else class="el-icon-plus avatar-uploader-icon"></i>
                    </el-upload>
                    <div class="upload-tip">只能上传jpg/png文件，且不超过2MB</div>
                </el-form-item>
            </el-form>
        </div>

        <div class="drawer-footer">
            <el-button @click="handleClose">取 消</el-button>
            <el-button type="primary" @click="handleSubmit" :loading="submitLoading">提 交</el-button>
        </div>
    </el-drawer>
</template>

<script>
import { userApis } from "@/api/user";
import { Message } from "element-ui";
import {Local} from "@/utils/storage";

const defaultForm = {
    id: null,
    username: '',
    nickName: '',
    password: '',
    avatarUrl: ''
};

export default {
    name: 'UserFormDrawer',
    props: {
        visible: {
            type: Boolean,
            default: false
        },
        mode: {
            type: String,
            validator: val => ['add', 'edit'].includes(val),
            default: 'add'
        },
        initialData: {
            type: Object,
            default: () => ({})
        }
    },
    data() {
        return {
            submitLoading: false,
            form: { ...defaultForm },
            uploadHeaders: {
                token: Local.get("token") ? Local.get('token') : ''
            },
        };
    },
    computed: {
        drawerVisible: {
            get() { return this.visible; },
            set(val) { this.$emit('update:visible', val); }
        },
        isEditMode() {
            return this.mode === 'edit';
        },
        title() {
            return this.isEditMode ? '编辑用户' : '新增用户';
        },
        passwordPlaceholder() {
            return this.isEditMode ? '留空则不修改密码' : '请输入密码';
        },
        rules() {
            const baseRules = {
                username: [
                    { required: true, message: '请输入用户名', trigger: 'blur' }
                ],
                nickName: [
                    { required: true, message: '请输入用户昵称', trigger: 'blur' }
                ],
                // avatarUrl: [
                //     { required: true, message: '请上传用户头像', trigger: 'change' }
                // ]
            };
            if (this.isEditMode) {
                return {
                    ...baseRules,
                    password: [
                        { min: 6, message: '密码长度不能少于6位', trigger: 'blur'}
                    ]
                }
            } else {
                return {
                    ...baseRules,
                    password: [
                        { required: true, message: '请输入密码', trigger: 'blur' },
                        { min: 6, message: '密码长度不能少于6位', trigger: 'blur'}
                    ]
                }
            }
        }
    },
    watch: {
        visible(newVal) {
            if (newVal) {
                this.$nextTick(() => {
                    this.$refs.userForm.clearValidate();
                });
                this.form = { ...defaultForm };
                if (this.isEditMode && this.initialData) {
                    this.form = JSON.parse(JSON.stringify(this.initialData));
                    this.form.password = '';
                }
            }
        }
    },
    methods: {
        resetForm() {
            this.form = { ...defaultForm };
            if (this.$refs.userForm) {
                this.$refs.userForm.resetFields();
            }
        },
        handleClose() {
            this.resetForm();
            this.drawerVisible = false;
        },
        handleAvatarSuccess(res) {
            if (res.code === 200) {
                this.form.avatarUrl = res.data;
                Message.success('头像上传成功！');
                this.$refs.userForm.validateField('avatarUrl');
            } else {
                if (res.code) {
                    Message.error(res.message || '上传失败！')
                }
            }
        },
        beforeAvatarUpload(file) {
            const isJPGorPNG = file.type === 'image/jpeg' || file.type === 'image/png';
            const isLt2M = file.size / 1024 / 1024 < 2;

            if (!isJPGorPNG) {
                this.$message.error('上传头像图片只能是 JPG/PNG 格式!');
            }
            if (!isLt2M) {
                this.$message.error('上传头像图片大小不能超过 2MB!');
            }
            return isJPGorPNG && isLt2M;
        },
        handleSubmit() {
            this.$refs.userForm.validate(async (valid) => {
                if (valid) {
                    this.submitLoading = true;
                    const dataToSubmit = { ...this.form };
                    if (this.isEditMode && !dataToSubmit.password) {
                        delete dataToSubmit.password;
                    }
                    try {
                        if (this.isEditMode) {
                            await userApis.updateUser(dataToSubmit);
                            Message.success('更新成功！');
                        } else {
                            await userApis.addUser(dataToSubmit);
                            Message.success('新增成功！');
                        }
                        this.$emit('success');
                        this.handleClose();
                    } catch (e) {
                        if (e.code) Message.error(e.message);
                    } finally {
                        this.submitLoading = false;
                    }
                }
            });
        }
    }
};
</script>

<style scoped>
.el-drawer__body {
    position: relative;
    display: flex;
    flex-direction: column;
    height: 100%;
    padding: 0;
}

.drawer-content {
    flex: 1 1 auto;
    overflow-y: auto;
    padding: 20px;
    padding-bottom: 70px; /* 预留底部按钮高度 */
}

.drawer-footer {
    position: absolute;
    bottom: 20px; /* 从底部往上浮动20px */
    left: 0;
    right: 0;
    height: 50px;
    padding: 10px 20px;
    /*border-top: 1px solid #e8e8e8;*/
    background: #fff;
    text-align: right;
    z-index: 10;
}

/* 使用 ::v-deep 或 >>> 来穿透 scoped 样式 */
.avatar-uploader ::v-deep .el-upload {
    border: 1px dashed #d9d9d9;
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
}
.avatar-uploader ::v-deep .el-upload:hover {
    border-color: #409EFF;
}
.avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 120px;
    height: 120px;
    line-height: 120px;
    text-align: center;
}
.avatar {
    width: 120px;
    height: 120px;
    display: block;
}

.upload-tip {
    font-size: 12px;
    color: #E6A23C; /* 修改为醒目的警告色 */
    margin-top: 5px;
    line-height: 1.5; /* 增加行高以改善可读性 */
}
</style>
