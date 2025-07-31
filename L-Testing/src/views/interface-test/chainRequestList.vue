<template>
    <div class="chain-request-container">
        <!-- 1. 整合后的搜索与操作区域 -->
        <div class="filter-container">
            <el-form :model="searchForm" label-width="80px">
                <el-row :gutter="20" class="search-row">
                    <el-col :span="6">
                        <el-form-item label="链路ID">
                            <el-input v-model="searchForm.id" placeholder="请输入链路ID" clearable></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="链路名称">
                            <el-input v-model="searchForm.name" placeholder="请输入链路请求名称" clearable></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="用例ID">
                            <el-input v-model="searchForm.caseIds" placeholder="多个ID用空格/逗号" clearable></el-input>
                        </el-form-item>
                    </el-col>
                    <el-col :span="6">
                        <el-form-item label="执行人">
                            <el-select v-model="searchForm.userId" placeholder="请选择执行人" clearable filterable style="width: 100%;">
                                <el-option
                                    v-for="user in users"
                                    :key="user.id"
                                    :label="user.nickName"
                                    :value="user.id"
                                ></el-option>
                            </el-select>
                        </el-form-item>
                    </el-col>
                </el-row>
                <el-row :gutter="20" class="search-row">
                    <el-col :span="8">
                        <el-form-item label="创建时间">
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

            <!-- 查询、重置、新增、批量删除按钮行 -->
            <div class="action-bar">
                <div class="left-buttons">
                    <el-button type="primary" icon="el-icon-search" @click="handleSearch">查询</el-button>
                    <el-button icon="el-icon-refresh" @click="handleReset">重置</el-button>
                </div>
                <div class="right-buttons">
                    <el-button type="primary" icon="el-icon-plus" @click="handleAddNew">新增链路</el-button>
                    <el-button type="danger" icon="el-icon-delete" @click="handleBatchDelete" :disabled="multipleSelection.length === 0">批量删除</el-button>
                </div>
            </div>
        </div>

        <!-- 2. 表格区域 -->
        <el-table
            v-loading="loading"
            :data="tableData"
            style="width: 100%;"
            @selection-change="handleSelectionChange"
            border
            :row-key="getRowKey"
            ref="table"
        >
            <el-table-column type="selection" width="55" align="center" reserve-selection></el-table-column>
            <el-table-column prop="id" label="ID" width="80" align="center"></el-table-column>
            <el-table-column prop="name" label="链路请求名称" width="200" show-overflow-tooltip></el-table-column>
            <el-table-column label="关联的用例ID" min-width="250">
                <template slot-scope="scope">
                    <el-tag
                        v-for="caseId in scope.row.caseIds"
                        :key="caseId"
                        size="small"
                        style="margin-right: 5px; margin-bottom: 5px;"
                    >
                        {{ caseId }}
                    </el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="createUserName" label="创建用户" width="120" align="center"></el-table-column>
            <el-table-column prop="createdTime" label="创建时间" width="160" align="center"></el-table-column>
            <el-table-column prop="updateUserName" label="更新用户" width="120" align="center"></el-table-column>
            <el-table-column prop="updatedTime" label="更新时间" width="160" align="center"></el-table-column>

            <!-- 操作列 -->
            <el-table-column label="操作" width="220" fixed="right" align="center">
                <template slot-scope="scope">
                    <el-button size="mini" type="text" icon="el-icon-video-play" @click="handleExecute(scope.row)" style="color: #5ec35f;">执行</el-button>
                    <el-button size="mini" type="text" icon="el-icon-edit" @click="handleEdit(scope.row)">编辑</el-button>
                    <el-button size="mini" type="text" icon="el-icon-delete" @click="handleDelete(scope.row)" style="color: #F56C6C;">删除</el-button>
                </template>
            </el-table-column>
        </el-table>

        <!-- 分页 -->
        <el-pagination
            style="margin-top: 20px; text-align: right;"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            :current-page="pagination.current"
            :page-sizes="[10, 20, 50, 100]"
            :page-size="pagination.size"
            layout="total, sizes, prev, pager, next, jumper"
            :total="pagination.total">
        </el-pagination>

        <chain-request-dialog
            :visible.sync="dialogVisible"
            :title="dialogTitle"
            :initial-data="dialogFormData"
            :submit-loading="isDialogSubmitting"
            @submit="handleSave"
        />
    </div>
</template>

<script>
import ChainRequestDialog from "@/views/interface-test/components/chainRequestDialog.vue";
import {chainRequestApis} from "@/api/interface/chainRequest";
import {Message} from "element-ui";
import {userApis} from "@/api/user";

export default {
    name: 'ChainRequestList',
    components: {
        ChainRequestDialog
    },
    data() {
        return {
            loading: false,
            users: [],
            // 搜索表单
            searchForm: {
                id: '',
                name: '',
                caseIds: '',
                userId: '',
                createdTimeRange: [],
            },
            // 表格数据
            tableData: [],
            // 多选数据
            multipleSelection: [],
            // 分页配置
            pagination: {
                current: 1,
                size: 10,
                total: 0,
            },
            // 对话框相关
            dialogVisible: false,
            dialogTitle: '',
            dialogFormData: null,
            isDialogSubmitting: false
        };
    },
    created() {
        this.fetchData();
        this.fetchActiveUser()
    },
    methods: {
        async fetchActiveUser() {
            try {
                const { data } = await userApis.getActiveUserList();
                this.users = data;
            } catch (e) {
                if (e.code) {
                    Message.error(e.message);
                }
            }
        },
        // 获取列表数据
        async fetchData() {
            this.loading = true;
            // 准备查询参数
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

            // 处理 "包含用例ID" 搜索条件
            // 支持按空格、中英文逗号
            if (this.searchForm.caseIds) {
                params.caseIds = this.searchForm.caseIds.split(/[,\s，]+/).filter(id => id).join(',');
            } else {
                params.caseIds = [];
            }

            try {
                const {data} = await chainRequestApis.getChainRequestList(params)
                this.pagination.total = data.total
                this.tableData = data.records
            } catch (e) {
                if (e.code) {
                    Message.error(e.message)
                }
            } finally {
                this.loading = false
            }
        },
        // 点击查询
        handleSearch() {
            this.pagination.current = 1;
            this.fetchData();
        },
        // 点击重置
        handleReset() {
            this.searchForm = {
                id: '',
                name: '',
                caseIds: '',
                creatorName: '',
                createTimeRange: [],
            };
            this.handleSearch();
        },
        getRowKey(row) {
            return row.id
        },
        // 表格多选变化
        handleSelectionChange(val) {
            this.multipleSelection = val;
        },
        // 分页大小变化
        handleSizeChange(val) {
            this.pagination.size = val;
            this.fetchData();
        },
        // 当前页码变化
        handleCurrentChange(val) {
            this.pagination.current = val;
            this.fetchData();
        },
        // 点击新增
        handleAddNew() {
            this.dialogTitle = '新增链路请求';
            this.dialogFormData = null; // 新增时，不传递初始数据
            this.dialogVisible = true;
        },
        async handleEdit(row) {
            this.dialogTitle = '编辑链路请求';
            const {data} = await chainRequestApis.getChainRequestDetail({id: row.id})

            // 传递整行数据给子组件
            this.dialogFormData = {
                id: data.id,
                name: data.name,
                caseIds: data.caseIds,
                interfaceAndCaseIds: data.interfaceAndCaseIds,
                selectedCases: data.selectedCaseNameList
            };
            this.dialogVisible = true;
        },
        // 点击删除
        handleDelete(row) {
            this.$confirm(`此操作将永久删除链路请求 [${row.name}], 是否继续?`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }).then(async () => {
                try {
                    await chainRequestApis.deleteChainRequest({ids: [row.id]})
                    Message.success('删除成功')
                    this.afterDeleteRefresh()
                } catch (e) {
                    if (e.code) {
                        Message.error(e.message)
                    }
                }
            }).catch(()=>{});
        },
        // 点击批量删除
        handleBatchDelete() {
            const ids = this.multipleSelection.map(item => item.id);
            this.$confirm(`此操作将永久删除选中的 ${ids.length} 条链路请求, 是否继续?`, '提示', {
                confirmButtonText: '确定',
                cancelButtonText: '取消',
                type: 'warning',
            }).then(async () => {
                try {
                    await chainRequestApis.deleteChainRequest({ids})
                    Message.success('删除成功')
                    this.afterDeleteRefresh()
                } catch (e) {
                    if (e.code) {
                        Message.error(e.message)
                    }
                }
            }).catch(() => {});
        },
        // 点击执行
        handleExecute(row) {
            this.$message.info(`开始执行链路: ${row.name}`);
            // 此处可以添加调用执行接口的逻辑
            console.log('执行:', row);
        },
        async handleSave(v) {
            this.isDialogSubmitting = true
            const {mode, data} = v
            try {
                if (mode === 'edit') {
                    await chainRequestApis.updateChainRequest(data)
                    Message.success("更新成功~")
                } else {
                    await chainRequestApis.saveChainRequest(data)
                    Message.success("保存成功~")
                }
                this.dialogVisible = false
                this.fetchData()
            } catch (e) {
                if (e.code) {
                    Message.error(e.message)
                }
            } finally {
                this.isDialogSubmitting = false
            }
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
.chain-request-container {
    padding: 20px;
    background-color: #fff; /* 添加白色背景，使其看起来像一个整体 */
    border-radius: 4px; /* 轻微的圆角 */
}
.filter-container {
    margin-bottom: 30px;
}
.filter-container .search-row {
    margin-bottom: 20px;
}
.action-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 10px; /* 在表单和操作栏之间增加一些间距 */
}
.el-tag + .el-tag {
    margin-left: 10px;
}
.form-tip {
    color: #909399;
    font-size: 12px;
    line-height: 1.5;
}
</style>
