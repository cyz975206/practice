<template>
  <div class="person-manage">
    <!-- 页面头部 -->
    <div class="pm-header">
      <div class="pm-header__left">
        <h2 class="pm-header__title">人员管理</h2>
        <span class="pm-header__count">共 <em>{{ total }}</em> 人</span>
      </div>
      <div class="pm-header__actions">
        <el-button type="success" @click="handleExport">
          <el-icon><Download /></el-icon>
          <span>导出</span>
        </el-button>
        <el-button type="warning" @click="handleImport">
          <el-icon><Upload /></el-icon>
          <span>导入</span>
        </el-button>
        <el-button type="primary" @click="handleAdd">
          <el-icon><Plus /></el-icon>
          <span>新增人员</span>
        </el-button>
      </div>
    </div>

    <!-- 搜索工具栏 -->
    <div class="pm-toolbar">
      <el-input
        v-model="queryParams.fullName"
        placeholder="搜索姓名..."
        clearable
        :prefix-icon="Search"
        class="pm-toolbar__name"
        @keyup.enter="handleQuery"
      />
      <el-input
        v-model="queryParams.staffNum"
        placeholder="搜索工号..."
        clearable
        class="pm-toolbar__staff"
        @keyup.enter="handleQuery"
      />
      <el-tree-select
        v-model="queryParams.orgCode"
        :data="orgTreeData"
        :props="{
          children: 'children',
          label: 'orgShortName',
          value: 'orgCode',
        }"
        node-key="orgCode"
        check-strictly
        :render-after-expand="false"
        placeholder="全部机构"
        clearable
        class="pm-toolbar__org"
      />
      <el-select
        v-model="queryParams.status"
        placeholder="全部状态"
        clearable
        class="pm-toolbar__status"
      >
        <el-option
          v-for="item in statusOptions"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-button @click="handleQuery">搜索</el-button>
      <el-button @click="resetQuery">重置</el-button>
    </div>

    <!-- 表格区域 -->
    <div class="pm-table-panel">
      <el-table v-loading="tableLoading" :data="tableData">
        <el-table-column prop="fullName" label="姓名" min-width="140">
          <template #default="{ row }">
            <div class="pm-person-cell">
              <span class="pm-person-name">{{ row.fullName }}</span>
              <span
                class="pm-status-badge"
                :class="`pm-status-badge--${personStatusTagType(row.status)}`"
              >
                {{ personStatusLabel(row.status) }}
              </span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="staffNum" label="工号" min-width="110">
          <template #default="{ row }">
            <code class="pm-staff-num">{{ row.staffNum }}</code>
          </template>
        </el-table-column>
        <el-table-column
          prop="orgCode"
          label="所属机构"
          min-width="140"
          show-overflow-tooltip
        />
        <el-table-column prop="phone" label="手机号" min-width="130">
          <template #default="{ row }">
            <code class="pm-phone">{{ maskPhone(row.phone) }}</code>
          </template>
        </el-table-column>
        <el-table-column
          prop="createTime"
          label="创建时间"
          width="170"
          align="center"
          show-overflow-tooltip
        >
          <template #default="{ row }">
            <span class="pm-time">{{ formatDate(row.createTime) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center" fixed="right">
          <template #default="{ row }">
            <div class="pm-actions">
              <el-tooltip content="编辑" placement="top">
                <el-button circle size="small" @click="handleEdit(row)">
                  <el-icon :size="14"><Edit /></el-icon>
                </el-button>
              </el-tooltip>
              <el-popconfirm
                :title="`确认删除人员「${row.fullName}」？`"
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="handleDeleteRow(row)"
              >
                <template #reference>
                  <el-button circle size="small" type="danger">
                    <el-icon :size="14"><Delete /></el-icon>
                  </el-button>
                </template>
              </el-popconfirm>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <Pagination
        v-if="total > 0"
        v-model:page="queryParams.page"
        v-model:limit="queryParams.size"
        :total="total"
        @pagination="loadTableData"
      />
    </div>

    <!-- 新增/编辑抽屉 -->
    <el-drawer
      v-model="dialogVisible"
      :title="dialogTitle"
      direction="rtl"
      size="480"
      :close-on-click-modal="false"
      append-to-body
      @close="resetForm"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-position="top"
        class="pm-form"
      >
        <div class="pm-form__section">
          <div class="pm-form__heading">基本信息</div>
          <div class="pm-form__inline">
            <el-form-item label="姓" prop="surname">
              <el-input v-model="formData.surname" placeholder="如 张" />
            </el-form-item>
            <el-form-item label="名" prop="givenName">
              <el-input v-model="formData.givenName" placeholder="如 三" />
            </el-form-item>
          </div>
          <el-form-item label="身份证号" prop="idCard">
            <el-input v-model="formData.idCard" placeholder="请输入身份证号" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="formData.phone" placeholder="请输入手机号" />
          </el-form-item>
        </div>
        <div class="pm-form__section">
          <div class="pm-form__heading">任职信息</div>
          <div class="pm-form__inline">
            <el-form-item label="工号" prop="staffNum">
              <el-input v-model="formData.staffNum" placeholder="如 E001" />
            </el-form-item>
            <el-form-item label="状态" prop="status">
              <el-select
                v-model="formData.status"
                placeholder="请选择状态"
                style="width: 100%"
              >
                <el-option
                  v-for="item in statusOptions"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-form-item>
          </div>
          <el-form-item label="所属机构" prop="orgCode">
            <el-tree-select
              v-model="formData.orgCode"
              :data="orgTreeData"
              :props="{
                children: 'children',
                label: 'orgShortName',
                value: 'orgCode',
              }"
              node-key="orgCode"
              check-strictly
              :render-after-expand="false"
              placeholder="请选择所属机构"
              clearable
              style="width: 100%"
            />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="pm-drawer__footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button
            type="primary"
            :loading="submitLoading"
            @click="handleSubmit"
          >
            确定
          </el-button>
        </div>
      </template>
    </el-drawer>

    <!-- 导入抽屉 -->
    <el-drawer
      v-model="importDialogVisible"
      title="导入人员"
      direction="rtl"
      size="440"
      :close-on-click-modal="false"
      append-to-body
    >
      <div class="pm-import">
        <el-upload
          ref="uploadRef"
          :auto-upload="false"
          :limit="1"
          accept=".xlsx,.xls"
          :on-exceed="handleExceed"
          :http-request="() => {}"
          drag
          class="pm-import__upload"
        >
          <el-icon class="el-icon--upload"><Upload /></el-icon>
          <div class="el-upload__text">
            将文件拖到此处，或 <em>点击上传</em>
          </div>
          <template #tip>
            <div class="el-upload__tip">仅支持 .xlsx / .xls 文件</div>
          </template>
        </el-upload>
      </div>
      <template #footer>
        <div class="pm-drawer__footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="importLoading" @click="submitImport">
            确认导入
          </el-button>
        </div>
      </template>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import {
  Plus,
  Edit,
  Delete,
  Download,
  Upload,
  Search,
} from '@element-plus/icons-vue'
import {
  getPersonPage,
  createPerson,
  updatePerson,
  deletePerson,
  exportPerson,
  importPerson,
} from '@/api/person'
import type { PersonVO, PersonForm, PersonQuery } from '@/api/person/types'
import { getOrgTree } from '@/api/org'
import type { OrgVO } from '@/api/org/types'
import Pagination from '@/components/Pagination/index.vue'
import { useTable } from '@/composables/useTable'
import { useFormDialog } from '@/composables/useFormDialog'
import { useExportImport } from '@/composables/useExportImport'
import { useDict } from '@/composables/useDict'
import { formatDate } from '@/utils'

const { options: statusOptions, getLabel } = useDict('PersonStatus')

// 人员状态样式映射（前端 UI 关注点）
const PERSON_STATUS_TAG_MAP: Record<string, string> = {
  '1': 'success',
  '2': 'danger',
  '3': 'warning',
}

function personStatusLabel(status: string) {
  return getLabel('PersonStatus', status)
}

function personStatusTagType(status: string) {
  return PERSON_STATUS_TAG_MAP[status] ?? 'info'
}

// ==================== 机构树 ====================
const orgTreeData = ref<OrgVO[]>([])

async function loadOrgTree() {
  try {
    const res = await getOrgTree()
    orgTreeData.value = res.data || []
  } catch (e) {
    console.error('加载机构树失败', e)
  }
}

// ==================== 表格 ====================
const {
  tableData,
  tableLoading,
  total,
  queryParams,
  loadTableData,
  handleQuery,
  resetQuery,
} = useTable<PersonQuery, PersonVO>({
  apiFn: getPersonPage,
  defaultQuery: {
    page: 1,
    size: 20,
    fullName: undefined,
    staffNum: undefined,
    orgCode: undefined,
    status: undefined,
  },
  immediate: false,
})

// ==================== 表单抽屉 ====================
const {
  dialogVisible,
  dialogTitle,
  submitLoading,
  formRef,
  formData,
  formRules,
  handleAdd,
  handleEdit,
  handleSubmit,
  resetForm,
} = useFormDialog<PersonForm>({
  defaultForm: {
    surname: '',
    givenName: '',
    idCard: '',
    phone: '',
    staffNum: '',
    orgCode: '',
    status: '1',
  },
  createTitle: '新增人员',
  editTitle: '编辑人员',
  createFn: createPerson,
  updateFn: updatePerson,
  formRules: {
    surname: [{ required: true, message: '请输入姓', trigger: 'blur' }],
    givenName: [{ required: true, message: '请输入名', trigger: 'blur' }],
    idCard: [{ required: true, message: '请输入身份证号', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
    staffNum: [{ required: true, message: '请输入工号', trigger: 'blur' }],
    orgCode: [{ required: true, message: '请选择所属机构', trigger: 'change' }],
    status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  },
  afterSubmit: loadTableData,
})

// ==================== 删除 ====================
async function handleDeleteRow(row: PersonVO) {
  try {
    await deletePerson(row.id)
    ElMessage.success('删除成功')
    loadTableData()
  } catch (e) {
    console.error('删除失败', e)
  }
}

// ==================== 导出导入 ====================
const {
  importDialogVisible,
  importLoading,
  uploadRef,
  handleExport,
  handleImport,
  handleExceed,
  submitImport,
} = useExportImport<PersonQuery>({
  entityName: '人员',
  exportFn: exportPerson,
  importFn: importPerson,
  queryParams,
  afterImport: loadTableData,
})

// ==================== 辅助方法 ====================
function maskPhone(phone?: string) {
  if (!phone || phone.length < 7) return phone || ''
  return phone.substring(0, 3) + '****' + phone.substring(7)
}

// ==================== 初始化 ====================
onMounted(() => {
  loadOrgTree()
  loadTableData()
})
</script>

<style lang="scss" scoped>
/* ─── 布局 ─── */
.person-manage {
  display: flex;
  flex-direction: column;
  gap: 16px;
  height: 100%;
  padding: 20px 24px;
  box-sizing: border-box;
}

/* ─── 头部 ─── */
.pm-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;

  &__left {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  &__title {
    margin: 0;
    font-size: 20px;
    font-weight: 700;
    color: var(--el-text-color-primary);
    letter-spacing: -0.02em;
  }

  &__count {
    font-size: 13px;
    color: var(--el-text-color-regular);
    background: var(--el-fill-color-light);
    border: 1px solid var(--el-border-color-extra-light);
    padding: 3px 12px;
    border-radius: 100px;

    em {
      font-style: normal;
      font-weight: 600;
      color: var(--el-color-primary);
    }
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }
}

/* ─── 工具栏 ─── */
.pm-toolbar {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;

  &__name {
    width: 180px;
  }

  &__staff {
    width: 160px;
  }

  &__org {
    width: 180px;
  }

  &__status {
    width: 130px;
  }
}

/* ─── 表格面板 ─── */
.pm-table-panel {
  flex: 1;
  background: var(--el-bg-color);
  border: 1px solid var(--el-border-color-lighter);
  border-radius: 8px;
  overflow: hidden;
  min-height: 0;

  :deep(.el-table) {
    --el-table-border-color: transparent;
    --el-table-header-border-color: transparent;

    th.el-table__cell {
      background: var(--el-fill-color-lighter);
      font-weight: 600;
      font-size: 12px;
      letter-spacing: 0.04em;
      color: var(--el-text-color-secondary);
    }

    td.el-table__cell {
      border-bottom-color: var(--el-border-color-extra-light);
    }

    .el-table__row {
      transition: background 0.15s;
    }

    .el-table__row:hover > td.el-table__cell {
      background: var(--el-fill-color-light) !important;
    }
  }

  :deep(.el-pagination) {
    padding: 16px 20px;
    display: flex;
    justify-content: flex-end;
    border-top: 1px solid var(--el-border-color-extra-light);
  }
}

/* ─── 表格单元格 ─── */
.pm-person-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.pm-person-name {
  font-weight: 500;
  color: var(--el-text-color-primary);
}

.pm-staff-num {
  font-size: 13px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  padding: 2px 8px;
  border-radius: 4px;
}

.pm-phone {
  font-size: 13px;
  font-family: 'SF Mono', 'Cascadia Code', 'JetBrains Mono', Menlo, monospace;
  color: var(--el-text-color-secondary);
  background: var(--el-fill-color);
  padding: 2px 8px;
  border-radius: 4px;
  letter-spacing: 0.05em;
}

.pm-time {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

/* ─── 状态标签 ─── */
.pm-status-badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 100px;
  font-size: 12px;
  font-weight: 500;

  &--success {
    background: #ecfdf5;
    color: #059669;
  }

  &--danger {
    background: #fef2f2;
    color: #dc2626;
  }

  &--info {
    background: var(--el-fill-color-light);
    color: var(--el-text-color-secondary);
  }

  &--warning {
    background: #fffbeb;
    color: #d97706;
  }
}

/* ─── 操作按钮 ─── */
.pm-actions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
}

/* ─── 表单 ─── */
.pm-form {
  &__section {
    padding-bottom: 24px;
    margin-bottom: 24px;
    border-bottom: 1px solid var(--el-border-color-lighter);

    &:last-child {
      border-bottom: none;
      margin-bottom: 0;
    }
  }

  &__heading {
    font-size: 12px;
    font-weight: 600;
    color: var(--el-text-color-secondary);
    text-transform: uppercase;
    letter-spacing: 0.08em;
    margin-bottom: 16px;
  }

  &__inline {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 0 16px;
  }
}

/* ─── 导入 ─── */
.pm-import {
  &__upload {
    :deep(.el-upload-dragger) {
      border-radius: 8px;
      padding: 32px 20px;
    }
  }
}

/* ─── 抽屉底部 ─── */
.pm-drawer__footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* ─── 暗色模式 ─── */
html.dark {
  .pm-staff-num,
  .pm-phone {
    background: rgba(255, 255, 255, 0.06);
  }

  .pm-status-badge--success {
    background: rgba(16, 185, 129, 0.15);
  }

  .pm-status-badge--danger {
    background: rgba(220, 38, 38, 0.12);
  }

  .pm-status-badge--warning {
    background: rgba(217, 119, 6, 0.12);
  }
}

/* ─── 响应式 ─── */
@media (max-width: 768px) {
  .pm-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .pm-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .pm-toolbar__name,
  .pm-toolbar__staff,
  .pm-toolbar__org,
  .pm-toolbar__status {
    width: 100%;
  }

  .pm-form__inline {
    grid-template-columns: 1fr;
  }
}
</style>
