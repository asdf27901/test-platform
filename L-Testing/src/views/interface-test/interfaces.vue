<template>
    <div class="interface-list-page">
        <!-- 1. 顶部搜索表单 (严格3列布局) -->
        <el-form :model="searchForm" ref="searchForm" label-width="100px" class="search-form">
            <el-row :gutter="24">
                <el-col :span="8">
                    <el-form-item label="创建用户" prop="userId">
                        <el-select v-model="searchForm.userId" placeholder="请选择创建用户" clearable style="width: 100%;" filterable>
                            <el-option
                                v-for="user in users"
                                :key="user.id"
                                :label="user.nickName"
                                :value="user.id"
                            ></el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item label="接口名称" prop="name">
                        <el-input v-model="searchForm.name" placeholder="请输入接口名称" clearable></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item label="请求方法" prop="method">
                        <el-select v-model="searchForm.method" placeholder="请选择方法" clearable style="width: 100%;">
                            <el-option
                                v-for="item in methodOptions"
                                :key="item.value"
                                :label="item.label"
                                :value="item.value"
                            >
                                <el-tag :type="getMethodTagType(item.value)" effect="dark">{{ item.label }}</el-tag>
                            </el-option>
                        </el-select>
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item label="接口路径" prop="path">
                        <el-input v-model="searchForm.path" placeholder="请输入接口路径" clearable></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item label="接口描述" prop="description">
                        <el-input v-model="searchForm.description" placeholder="请输入接口描述" clearable></el-input>
                    </el-form-item>
                </el-col>
                <el-col :span="8">
                    <el-form-item label="创建时间" prop="createdTimeRange">
                        <el-date-picker
                                v-model="searchForm.createdTimeRange"
                                type="datetimerange"
                                range-separator="至"
                                start-placeholder="开始日期"
                                end-placeholder="结束日期"
                                value-format="yyyy-MM-dd HH:mm:ss"
                                style="width: 100%;">
                        </el-date-picker>
                    </el-form-item>
                </el-col>
            </el-row>
        </el-form>

        <!-- 2. 顶部操作区 -->
        <div class="actions-bar">
            <!-- 左侧搜索操作 -->
            <div class="search-actions">
                <el-button type="primary" icon="el-icon-search" @click="handleSearch">搜索</el-button>
                <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
            </div>
            <!-- 右侧主要操作 -->
            <div class="main-actions">
                <el-button type="primary" icon="el-icon-plus" @click="handleCreate">创建接口</el-button>
                <el-button
                        type="danger"
                        icon="el-icon-delete"
                        @click="handleBatchDelete"
                        :disabled="multipleSelection.length === 0"
                >
                    批量删除
                </el-button>
            </div>
        </div>

        <!-- 3. 数据表格 -->
        <el-table
                v-loading="loading"
                :data="tableData"
                style="width: 100%"
                border
                @selection-change="handleSelectionChange"
                :header-cell-style="{ background: '#fafafa', color: '#606266', textAlign: 'center' }"
                cell-class-name="table-cell-center"
                :row-key="getRowKey"
        >
            <el-table-column type="selection" width="55" align="center" reserve-selection></el-table-column>
            <el-table-column prop="id" label="接口ID" width="80" align="center"></el-table-column>
            <el-table-column prop="name" label="接口名称" width="200" show-overflow-tooltip align="center"></el-table-column>
            <el-table-column prop="method" label="请求方法" width="120" align="center">
                <template #default="{row}">
                    <el-tag :type="getMethodTagType(row.method)" effect="dark">{{ row.method }}</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="path" label="接口路径" width="250" show-overflow-tooltip align="center"></el-table-column>
            <el-table-column prop="description" label="接口描述" show-overflow-tooltip align="center" width="400"></el-table-column>
            <el-table-column prop="createUser" label="创建用户" width="100" align="center"></el-table-column>
            <el-table-column prop="updateUser" label="更新用户" width="120" align="center"></el-table-column>
            <el-table-column prop="createdTime" label="创建时间" width="180" align="center"></el-table-column>
            <el-table-column prop="updatedTime" label="更新时间" width="180" align="center"></el-table-column>

            <el-table-column label="操作" width="290" fixed="right" align="center">
                <template #default="{row}">
                    <el-button size="mini" type="primary" icon="el-icon-edit" @click="handleEdit(row)">编辑
                    </el-button>
                    <el-button size="mini" type="danger" icon="el-icon-delete" @click="handleDelete(row)">删除
                    </el-button>
                    <el-button size="mini" type="success" icon="el-icon-document-add"
                               @click="handleGenerateCase(row)">生成用例
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 4. 分页 -->
        <el-pagination
                class="pagination"
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page="pagination.currentPage"
                :page-sizes="[10, 20, 50, 100]"
                :page-size="pagination.size"
                layout="total, sizes, prev, pager, next, jumper"
                :total="pagination.total">
        </el-pagination>

        <create-interface-dialog
            :visible.sync="createDialogVisible"
            @success="handleCreateSuccess"
        />

        <edit-interface-dialog
            v-if="editDialogVisible"
            :visible.sync="editDialogVisible"
            :interface-data="currentEditingInterface"
            @success="handleEditSuccess"
        />
    </div>
</template>

<script>
import createInterfaceDialog from "@/views/interface-test/components/createInterfaceDialog.vue";
import {interfaceApis} from "@/api/interface";
import {Message} from "element-ui";
import {userApis} from "@/api/user";
import editInterfaceDialog from "@/views/interface-test/components/editInterfaceDialog.vue";

export default {
    name: 'InterfaceList',
    components: {
      createInterfaceDialog, editInterfaceDialog
    },
    data() {
        return {
            // 搜索表单数据
            searchForm: {
                userId: '',
                name: '',
                method: '',
                path: '',
                description: '',
                createdTimeRange: [], // [startTime, endTime]
            },
            // 表格加载状态
            loading: false,
            // 表格数据
            tableData: [],
            // 多选中的数据
            multipleSelection: [],
            allSelectedIds: new Set(),
            // 分页信息
            pagination: {
                current: 1,
                size: 10,
                total: 0,
            },
            users: [],
            // 请求方法选项
            methodOptions: [
                {value: 'GET', label: 'GET'},
                {value: 'POST', label: 'POST'},
                {value: 'PUT', label: 'PUT'},
                {value: 'DELETE', label: 'DELETE'},
                {value: 'PATCH', label: 'PATCH'},
            ],
            createDialogVisible: false,
            editDialogVisible: false,      // 控制编辑弹窗的显示
            currentEditingInterface: {},   // 存储当前正在编辑的行数据
        };
    },
    async created() {
        await this.fetchActiveUser()
        await this.fetchData();
    },
    methods: {
        // 获取数据
        async fetchData() {
            this.loading = true;
            try {
                const params = {
                    current: this.pagination.current,
                    size: this.pagination.size,
                };
                // 清理搜索表单中的空值
                for (const key in this.searchForm) {
                    if (this.searchForm[key] !== '' && this.searchForm[key] !== null && this.searchForm[key].length !== 0) {
                        params[key] = this.searchForm[key];
                    }
                }
                if (params.createdTimeRange && params.createdTimeRange.length === 2) {
                    params.begin = params.createdTimeRange[0];
                    params.end = params.createdTimeRange[1];
                }
                delete params.createdTimeRange;

                const {data} = await interfaceApis.getInterfaceList(params);
                this.tableData = data.records
                this.pagination.total = data.total
            } catch (error) {
                if (error.code) {
                    Message.error(error.message)
                }
            } finally {
                this.loading = false;
            }
        },

        async fetchActiveUser() {
            try {
                const {data} = await userApis.getActiveUserList()
                this.users = data
            }catch (error) {
                if (error.code) {
                    Message.error(error.message)
                }
            }

        },

        // 搜索
        handleSearch() {
            this.pagination.current = 1;
            this.fetchData();
        },

        // 重置搜索表单
        handleReset() {
            this.$refs.searchForm.resetFields();
            this.searchForm.createdTimeRange = []; // resetFields 对 el-date-picker 可能无效，手动清空
            this.handleSearch();
        },

        // 表格多选变化
        handleSelectionChange(val) {
            this.multipleSelection = val
        },

        // 分页：每页数量变化
        handleSizeChange(val) {
            this.pagination.size = val;
            this.pagination.current = 1
            this.fetchData();
        },

        // 分页：当前页变化
        handleCurrentChange(val) {
            this.pagination.current = val;
            this.fetchData();
        },

        // 编辑接口
        handleEdit(row) {
            // 使用深拷贝或浅拷贝，防止弹窗中的修改直接影响表格行数据
            this.currentEditingInterface = { ...row };
            this.editDialogVisible = true;
        },

        // 删除接口
        handleDelete(row) {
            this.$confirm(`此操作将永久删除接口 "${row.name}", 是否继续?`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }).then(async () => {
                try {
                    await interfaceApis.deleteInterfacesBatch([row.id])
                    this.$message.success('删除成功!');
                    await this.afterDeleteRefresh()
                } catch (error) {
                    if (error.code) {
                        Message.error(error.message)
                    }
                }
            }).catch(()=>{});
        },

        // 批量删除
        handleBatchDelete() {
            if (this.multipleSelection.length === 0) {
                this.$message.warning('请至少选择一项进行删除');
                return;
            }
            this.$confirm(`此操作将永久删除选中的 ${this.multipleSelection.length} 个接口, 是否继续?`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }).then(async () => {
                try {
                    const ids = this.multipleSelection.map(item => item.id);
                    await interfaceApis.deleteInterfacesBatch(ids)
                    this.$message.success('批量删除成功!');
                    await this.afterDeleteRefresh()
                }catch (error) {
                    if (error.code) {
                        Message.error(error.message)
                    }
                }
            }).catch(() => {})
        },

        // 生成测试用例
        handleGenerateCase(row) {
            this.$message.success(`开始为接口 "${row.name}" 生成测试用例`);
        },

        // 根据请求方法返回不同的Tag类型
        getMethodTagType(method) {
            switch (method.toUpperCase()) {
                case 'GET':
                    return 'success';
                case 'POST':
                    return ''; // Element-UI 默认 primary
                case 'PUT':
                    return 'warning';
                case 'DELETE':
                    return 'danger';
                case 'PATCH':
                    return 'info';
                default:
                    return 'info';
            }
        },
        getRowKey(row) {
            return row.id
        },

        handleCreate() {
            this.createDialogVisible = true; // 只需改变状态，打开弹窗
        },

        // 5. 新增方法，用于处理子组件创建成功后的回调
        handleCreateSuccess() {
            this.createDialogVisible = false; // 关闭弹窗
            this.fetchData(); // 刷新列表数据
        },

        handleEditSuccess() {
            this.editDialogVisible = false;
            this.fetchData(); // 刷新列表数据
        },

        async afterDeleteRefresh() {
            await this.fetchData();

            const lastPage = Math.max(1, Math.ceil(this.pagination.total / this.pagination.size));

            if (this.pagination.current > lastPage) {
                this.pagination.current = lastPage;
                await this.fetchData();
            }
        }
    },
};
</script>

<style scoped>
.interface-list-page {
    padding: 20px;
    background-color: #fff; /* 改为白色 */
    min-height: calc(100vh - 50px);
}

/*.search-form .el-row {*/
/*    margin-bottom: 20px;*/
/*}*/

.search-form .el-col {
    margin-bottom: 20px;
}

.search-form .el-form-item {
    margin-bottom: 20px;
}

/* 新增：操作栏容器，使用Flexbox实现两端对齐 */
.actions-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px; /* 与下方表格的间距 */
}

.pagination {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
}
</style>
