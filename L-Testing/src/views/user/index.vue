<template>
    <!-- [修改] 容器设置为flex布局 -->
    <div class="page-container">
        <!-- 1. 搜索与操作区域 -->
        <div class="filter-container">
            <el-form :inline="true" :model="searchForm" class="filter-form">
                <el-form-item label="用户名">
                    <el-input v-model="searchForm.username" placeholder="请输入用户名" clearable @keyup.enter.native="handleQuery"></el-input>
                </el-form-item>
                <el-form-item label="用户昵称">
                    <el-input v-model="searchForm.nickName" placeholder="请输入用户昵称" clearable @keyup.enter.native="handleQuery"></el-input>
                </el-form-item>
                <el-form-item label="创建时间">
                    <el-date-picker
                        v-model="searchForm.creationTimeRange"
                        type="datetimerange"
                        range-separator="至"
                        start-placeholder="开始日期"
                        end-placeholder="结束日期"
                        value-format="yyyy-MM-dd HH:mm:ss"
                    >
                    </el-date-picker>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" icon="el-icon-search" @click="handleQuery">查询</el-button>
                    <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
                </el-form-item>
            </el-form>
        </div>

        <!-- 新增按钮 -->
        <el-button
            type="primary"
            icon="el-icon-plus"
            @click="handleAdd"
            style="margin-bottom: 20px;"
            v-if="username === 'admin'"
        >
            新增用户
        </el-button>

        <!-- 2. 用户列表区域 -->
        <div class="table-wrapper" ref="tableWrapper">
            <el-table
                :data="userList"
                v-loading="loading"
                :height="tableHeight"
                style="width: 100%;"
            >
                <el-table-column prop="id" label="用户ID" width="100" align="center"></el-table-column>
                <el-table-column prop="username" label="用户名" align="center"></el-table-column>
                <el-table-column label="用户头像" align="center">
                    <template slot-scope="scope">
                        <el-avatar :src="scope.row.avatarUrl ? scope.row.avatarUrl : require('@/assets/default_avatar.png')" :size="50"></el-avatar>
                    </template>
                </el-table-column>
                <el-table-column prop="nickName" label="用户昵称" min-width="100" align="center"></el-table-column>
                <el-table-column prop="active" label="用户状态" align="center">
                    <template #default="{row}">
                        <el-tag :type="row.active === 1 ? 'success' : 'danger'">{{ row.active === 1 ? '激活' : '禁用' }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createdTime" label="创建时间" align="center"></el-table-column>
                <el-table-column prop="updatedTime" label="更新时间" align="center"></el-table-column>
                <el-table-column label="操作" align="center" v-if="username === 'admin'">
                    <template #default="{row}">
                        <el-link
                            :type="row.active === 1 ? 'danger' : 'success'"
                            :icon="row.active === 1 ? 'el-icon-delete' : 'el-icon-refresh'"
                            :underline="false"
                            @click.stop="changeUserStatus(row.id, row.active === 1 ? 0 : 1)"
                            v-if="row.username !== 'admin'"
                        >
                            {{ row.active === 1 ? '禁用' : '激活' }}
                        </el-link>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <!-- 3. 分页区域 -->
        <div class="pagination-container" ref="paginationContainer">
            <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="pagination.current"
                :page-sizes="[10, 20, 50, 100]"
                :page-size="pagination.size"
                layout="total, sizes, prev, pager, next, jumper"
                :total="pagination.total">
            </el-pagination>
        </div>
        <!-- 4. 新增/编辑用户的抽屉组件 -->
        <user-form-drawer
            :visible.sync="drawerVisible"
            :mode="drawerMode"
            :initial-data="currentUser"
            @success="handleFormSuccess"
        ></user-form-drawer>
    </div>
</template>

<script>
import { userApis } from "@/api/user";
import { Message } from "element-ui";
import { debounce } from 'lodash';
import UserFormDrawer from "@/views/user/components/userFormDrawer.vue";

export default {
    name: 'UserManagement',
    components: {
        UserFormDrawer
    },
    data() {
        return {
            loading: false,
            searchForm: {
                username: '',
                nickName: '',
                creationTimeRange: [],
            },
            userList: [],
            pagination: {
                current: 1,
                size: 10,
                total: 0
            },
            tableHeight: 400,
            debouncedResizeHandler: null,

            // 为抽屉组件管理状态
            drawerVisible: false,
            drawerMode: 'add', // 'add' 或 'edit'
            currentUser: null,  // 存储当前正在编辑的用户数据
        };
    },
    created() {
        this.fetchUserList();
        this.debouncedResizeHandler = debounce(this.resizeTableHeight, 100);
    },
    mounted() {
        this.$nextTick(() => {
            this.resizeTableHeight();
        });
        window.addEventListener('resize', this.debouncedResizeHandler);
    },
    beforeDestroy() {
        window.removeEventListener('resize', this.debouncedResizeHandler);
    },
    methods: {
        resizeTableHeight() {
            if (this.$refs.tableWrapper) {
                const tableTop = this.$refs.tableWrapper.getBoundingClientRect().top;
                const paginationHeight = this.$refs.paginationContainer.offsetHeight;
                // 20是page-container的padding-bottom, 20是预留的额外底部间距
                const availableHeight = window.innerHeight - tableTop - paginationHeight - 20 - 20;
                this.tableHeight = Math.max(availableHeight, 200);
            }
        },
        async fetchUserList() {
            this.loading = true;
            const params = {
                current: this.pagination.current,
                size: this.pagination.size
            };
            // 清理搜索表单中的空值
            for (const key in this.searchForm) {
                if (this.searchForm[key] !== '' && this.searchForm[key] !== null && this.searchForm[key].length !== 0) {
                    params[key] = this.searchForm[key];
                }
            }

            if (params.creationTimeRange && params.creationTimeRange.length === 2) {
                params.begin = params.creationTimeRange[0];
                params.end = params.creationTimeRange[1];
            }
            delete params.creationTimeRange;
            try {
                const { data } = await userApis.getUserList(params);
                this.userList = data.records;
                this.pagination.total = data.total;
            } catch (e) {
                if (e.code) {
                    Message.error(e.message);
                }
            } finally {
                this.loading = false;
                this.$nextTick(() => {
                    this.resizeTableHeight();
                });
            }
        },
        handleQuery() {
            this.pagination.current = 1;
            this.fetchUserList();
        },
        handleReset() {
            this.searchForm = {
                username: '',
                nickName: '',
                creationTimeRange: [],
            };
            this.pagination.current = 1;
            this.fetchUserList();
        },
        handleAdd() {
            this.currentUser = null;
            this.drawerVisible = true;
        },
        handleSizeChange(val) {
            this.pagination.size = val;
            this.pagination.current = 1;
            this.fetchUserList();
        },
        handleCurrentChange(val) {
            this.pagination.current = val;
            this.fetchUserList();
        },
        handleFormSuccess() {
            this.pagination.current = 1;
            this.fetchUserList();
        },
        async changeUserStatus(id, active) {
            try {
                await userApis.changeActive({id, active})
                Message.success("修改成功");
                this.fetchUserList()
            }catch (e) {
                if (e.code) {
                    Message.error(e.message)
                }
            }
        }
    },
    computed: {
        username() {
            return this.$store.state.userInfos.userInfos.username
        }
    }
};
</script>

<style scoped>
.page-container {
    display: flex;
    flex-direction: column;
    height: 100%;
    padding: 20px;
    background-color: #fff;
    border-radius: 4px;
    box-sizing: border-box;
}

.filter-container {
    flex-shrink: 0;
    padding-bottom: 20px;
}

.page-container > .el-button {
    flex-shrink: 0;
    align-self: flex-start; /* 关键修复点 */
}

.table-wrapper {
    flex: 1;
    overflow: hidden;
}

.pagination-container {
    flex-shrink: 0;
    margin-top: 20px;
    text-align: right;
}

.filter-form .el-form-item {
    margin-bottom: 0;
    margin-right: 15px;
}
</style>
