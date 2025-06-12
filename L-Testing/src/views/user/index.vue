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
                    <el-input v-model="searchForm.nickname" placeholder="请输入用户昵称" clearable @keyup.enter.native="handleQuery"></el-input>
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
                <el-table-column prop="createdTime" label="创建时间" align="center"></el-table-column>
                <el-table-column prop="updatedTime" label="更新时间" align="center"></el-table-column>
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
    </div>
</template>

<script>
import { userApis } from "@/api/user";
import { Message } from "element-ui";
import { debounce } from 'lodash';

export default {
    name: 'UserManagement',
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
            debouncedResizeHandler: null
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
            const { username, nickName } = this.searchForm;
            const { current, size } = this.pagination;
            const params = {
                username,
                nickName,
                begin: this.searchForm.creationTimeRange ? this.searchForm.creationTimeRange[0] : null,
                end: this.searchForm.creationTimeRange ? this.searchForm.creationTimeRange[1] : null,
                current,
                size
            };
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
            this.$message.success('触发了新增用户操作！');
        },
        handleSizeChange(val) {
            this.pagination.size = val;
            this.pagination.current = 1;
            this.fetchUserList();
        },
        handleCurrentChange(val) {
            this.pagination.current = val;
            this.fetchUserList();
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
