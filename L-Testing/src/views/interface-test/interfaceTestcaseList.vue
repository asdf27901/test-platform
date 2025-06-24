<template>
    <div class="page-container">
        <!-- 1. 顶部搜索表单 -->
        <div class="filter-container">
            <el-form :model="searchForm" ref="searchFormRef" label-width="80px">
                <el-row :gutter="20">
                    <el-col :span="5">
                        <el-form-item label="用例ID" prop="id">
                            <el-input v-model.number="searchForm.id" placeholder="请输入用例ID" clearable></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="请求路径" prop="requestPath">
                            <el-input v-model="searchForm.path" placeholder="请输入请求路径" clearable></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="5">
                        <el-form-item label="请求方法" prop="method">
                            <el-select v-model="searchForm.method" placeholder="请选择" clearable style="width: 100%;">
                                <el-option label="GET" value="GET"></el-option>
                                <el-option label="POST" value="POST"></el-option>
                                <el-option label="PUT" value="PUT"></el-option>
                                <el-option label="DELETE" value="DELETE"></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                    <el-col :span="8">
                        <el-form-item label="用例名称" prop="name">
                            <el-input v-model="searchForm.name" placeholder="请输入用例名称" clearable></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="5">
                        <el-form-item label="接口ID" prop="interfaceId">
                            <el-input v-model.number="searchForm.interfaceId" placeholder="请输入接口ID" clearable></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
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
                    <el-col :span="5">
                        <el-form-item label="优先级" prop="priority">
                            <el-select v-model="searchForm.priority" placeholder="请选择" clearable style="width: 100%;">
                                <el-option label="高" :value="0"></el-option>
                                <el-option label="中" :value="1"></el-option>
                                <el-option label="低" :value="2"></el-option>
                            </el-select>
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
        </div>

        <!-- 2. 操作区 -->
        <div class="actions-bar">
            <!-- 左侧搜索操作 -->
            <div class="search-actions">
                <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
                <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
            </div>
            <!-- 右侧主要操作 -->
            <div class="main-actions">
                <el-button type="primary" icon="el-icon-plus" @click="handleAdd">新增用例</el-button>
                <el-button type="danger" icon="el-icon-delete" @click="handleBatchDelete" :disabled="selectedRows.length === 0">
                    批量删除
                </el-button>
            </div>
        </div>

        <!-- 3. 数据表格 -->
        <div class="table-wrapper" ref="tableWrapper">
            <el-table
                    :data="tableData"
                    v-loading="loading"
                    style="width: 100%"
                    @selection-change="handleSelectionChange"
                    :row-key="getRowKey"
                    border
                    stripe
                    :header-cell-style="{background:'#fafafa',color:'#606266', textAlign: 'center'}"
                    cell-class-name="table-cell-center"
                    :height="tableHeight"
                    ref="table"
            >
                <el-table-column type="selection" width="55" align="center" reserve-selection></el-table-column>
                <el-table-column prop="id" label="用例ID" width="80" align="center"></el-table-column>
                <el-table-column prop="interfaceId" label="接口ID" width="80" align="center"></el-table-column>
                <el-table-column prop="name" label="用例名称" min-width="180" show-overflow-tooltip align="center"></el-table-column>
                <el-table-column prop="method" label="请求方法" width="100" align="center">
                    <template slot-scope="scope">
                        <el-tag :type="getMethodTagType(scope.row.method)">{{ scope.row.method }}</el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="path" label="请求路径" min-width="200" show-overflow-tooltip align="center"></el-table-column>
                <el-table-column prop="priority" label="优先级" width="100" align="center">
                    <template slot-scope="scope">
                        <el-tag :type="getPriorityTagType(scope.row.priority)" size="medium">
                            {{ formatPriority(scope.row.priority) }}
                        </el-tag>
                    </template>
                </el-table-column>
                <el-table-column prop="createUsername" label="创建用户" width="120" align="center"></el-table-column>
                <el-table-column prop="createdTime" label="创建时间" width="160" align="center"></el-table-column>
                <el-table-column prop="updateUsername" label="更新用户" width="120" align="center"></el-table-column>
                <el-table-column prop="updatedTime" label="更新时间" width="160" align="center"></el-table-column>

                <el-table-column label="操作" width="150" align="center" fixed="right">
                    <template slot-scope="scope">
                        <el-button type="text" size="small" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
                        <el-button type="text" size="small" icon="el-icon-delete" @click="handleDelete(scope.row.id)" style="color: #F56C6C;">删除</el-button>
                    </template>
                </el-table-column>
            </el-table>
        </div>

        <!-- 4. 分页 -->
        <div class="pagination-container" ref="paginationContainer">
            <el-pagination
                    style="margin-top: 20px; text-align: right;"
                    :current-page="pagination.current"
                    :page-sizes="[10, 20, 50, 100]"
                    :page-size="pagination.size"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="pagination.total"
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
            ></el-pagination>

            <SelectInterfaceDialog
                :visible.sync="selectInterfaceDialogVisible"
                @success="handleSelectInterfaceSuccess"
            />
        </div>
    </div>
</template>

<script>
import { debounce } from 'lodash';
import {userApis} from "@/api/user";
import {Message} from "element-ui";
import SelectInterfaceDialog from "@/views/interface-test/components/selectInterfaceDialog.vue";
import {interfaceTestcaseApis} from "@/api/interface/interfaceTestcase";

export default {
    name: 'InterfaceTestcaseList',
    components: {
      SelectInterfaceDialog
    },
    data() {
        return {
            tableHeight: 400, // 默认高度
            debouncedResizeHandler: null,
            loading: false,
            searchForm: {
                id: null,
                interfaceId: null,
                name: '',
                method: '',
                path: '',
                priority: null,
                userId: '',
                createdTimeRange: [], // [startDate, endDate]
            },
            tableData: [], // 表格数据
            users: [],
            selectedRows: [], // 选中的行
            pagination: {
                current: 1,
                size: 10,
                total: 0,
            },
            selectInterfaceDialogVisible: false,
        };
    },
    created() {
        // 创建事件总线监听
        this.bus.$on('reFreshTestcaseList', () => {
            this.fetchData()
        })
        this.debouncedResizeHandler = debounce(this.resizeTableHeight, 100);
        this.fetchData();
        this.fetchActiveUser()
    },
    activated() {
      this.$nextTick(() => {
          this.resizeTableHeight()
      })
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

            // 卫兵：如果组件的根元素不在 DOM 渲染树中（比如在后台标签页），就直接返回，不进行计算
            if (!this.$el || !this.$el.offsetParent) {
                return;
            }

            if (this.$refs.tableWrapper && this.$refs.paginationContainer) {
                const tableTop = this.$refs.tableWrapper.getBoundingClientRect().top;
                const paginationHeight = this.$refs.paginationContainer.offsetHeight;
                const availableHeight = window.innerHeight - tableTop - paginationHeight - 20 - 20;
                this.tableHeight = Math.max(availableHeight, 200);
                if (this.$refs.table) {
                    this.$nextTick(() => {
                        this.$refs.table.doLayout();
                    });
                }
            }
        },
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
                const { data } = await interfaceTestcaseApis.getInterfaceTestcaseList(params)
                this.pagination.total = data.total
                this.tableData = data.records

            } catch (error) {
                if (error.code) {
                    Message.error(error.message)
                }
            } finally {
                this.loading = false;
                this.$nextTick(() => {
                    this.resizeTableHeight();
                });
            }
        },
        handleSearch() {
            this.pagination.current = 1;
            this.fetchData();
        },
        handleReset() {
            this.$refs.table.clearSelection()
            this.searchForm = {
                id: null,
                interfaceId: null,
                name: '',
                method: '',
                path: '',
                priority: null,
                userId: '',
                createdTimeRange: [],
            };
            this.handleSearch();
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
        handleAdd() {
            this.selectInterfaceDialogVisible = true
        },
        handleEdit(row) {
            // 注意：这里跳转的是用例编辑页
            this.$router.push({ name: 'EditInterfaceTestCase', query: { testcaseId: row.id } });
        },
        handleDelete(id) {
            this.$confirm('此操作将永久删除该测试用例, 是否继续?', '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }).then(async () => {
                try {
                    await interfaceTestcaseApis.deleteInterfaceTestcaseBatch([id])
                    this.$message.success('删除成功!')
                    await this.afterDeleteRefresh()
                } catch (e) {
                    if (e.code) {
                        Message.error(e.message)
                    }
                }
            }).catch(() => {});
        },
        handleBatchDelete() {
            if (this.selectedRows.length === 0) {
                this.$message.warning('请至少选择一条数据！');
                return;
            }
            this.$confirm(`确认删除选中的 ${this.selectedRows.length} 条测试用例吗?`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }).then(async () => {
                try {
                    const ids = this.selectedRows.map(item => item.id)
                    await interfaceTestcaseApis.deleteInterfaceTestcaseBatch(ids)
                    this.$message.success('批量删除成功!');
                    await this.afterDeleteRefresh()
                } catch (e) {
                    if (e.code) {
                        Message.error(e.message)
                    }
                }
            }).catch(() => {})
        },
        getRowKey(row) {
            return row.id
        },
        handleSelectionChange(selection) {
            this.selectedRows = selection;
        },
        handleSizeChange(val) {
            this.pagination.size = val;
            this.fetchData();
        },
        handleCurrentChange(val) {
            this.pagination.current = val;
            this.fetchData();
        },
        formatPriority(priority) {
            const priorityMap = { 0: '高', 1: '中', 2: '低' };
            return priorityMap[priority] || '未知';
        },
        getPriorityTagType(priority) {
            const tagTypeMap = { 0: 'danger', 1: 'warning', 2: 'info' };
            return tagTypeMap[priority] || 'info';
        },
        getMethodTagType(method) {
            const tagMap = { 'GET': 'success', 'POST': '', 'PUT': 'warning', 'DELETE': 'danger' };
            return tagMap[method.toUpperCase()] || '';
        },
        handleSelectInterfaceSuccess(interfaceId) {
            this.$router.push({
                name: 'AddInterfaceTestCase',
                query: { interfaceId: interfaceId } // 通过 query 参数传递ID
            });
        },
        async afterDeleteRefresh() {
            this.$refs.table.clearSelection()
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
}

/* 确保 el-col 在换行时有下边距 */
.filter-container .el-col {
    margin-bottom: 20px;
}

.actions-bar {
    flex-shrink: 0;
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
}

.table-wrapper {
    flex: 1;
    overflow: hidden;
}

.pagination-container {
    flex-shrink: 0;
    padding-top: 20px; /* 改为padding-top以避免影响高度计算 */
    text-align: right;
}

</style>
